package predict;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.MethodAccessor_Short;
import helper.*;
import libsvm.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by YU Fan on 2017/7/8.
 * 预测后一日的涨跌情况（close-open<0为跌，close-open>0为涨）
 * 结果为1：涨，-1：跌，0：平（基本不可能）
 */
public class ChangePredict extends Operation{
    /**取“一段时间”来预测，这里为7天*/
    public static final int days=7;
    
    /**训练集，目前用2016年一年的数据来训练*/
    public List<Mash> train(String year)
    {
        List<Mash> MashList = new ArrayList<Mash>();
        ResultSet resultSet = null;
        Connector conn = new Connector();
        try {
            resultSet = super.select(conn, "select * from mashData " + "where dataTime Like '"+year+"%' and symbol='SH600000' order by dataTime");
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<HashMap<String, Object>> list = null;
        try {
            list = QueryTool.resultSetToList(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < list.size(); i++) {
            HashMap<String, Object> map = list.get(i);
            Mash mash=new Mash();
            mash.setId((int)(long)map.get("id"));
            mash.setSymbol((String)map.get("symbol"));
            mash.setMa5Price((double)map.get("ma5_price"));
            mash.setMa5Volume((double)map.get("ma5_volume"));
            mash.setMa10Price((double)map.get("ma10_price"));
            mash.setMa10Volume((double)map.get("ma10_volume"));
            mash.setMa20Price((double)map.get("ma20_price"));
            mash.setMa20Volume((double)map.get("ma20_volume"));
            mash.setDiff((double)map.get("diff"));
            mash.setDea((double)map.get("dea"));
            mash.setMacd((double)map.get("macd"));
            mash.setK((double)map.get("k"));
            mash.setD((double)map.get("d"));
            mash.setJ((double)map.get("j"));
            mash.setRsi1((double)map.get("rsi1"));
            mash.setRsi2((double)map.get("rsi2"));
            mash.setRsi3((double)map.get("rsi3"));
            mash.setDate(map.get("dataTime").toString());
            MashList.add(mash);
        }
        conn.close();
        return MashList;
    }
    /**用于预测的数据集，即时间长度为“days”的数据*/
    public List<Mash> predict()
    {
        List<Mash> MashList = new ArrayList<Mash>();
        ResultSet resultSet = null;
        Connector conn = new Connector();
        try {
            resultSet = super.select(conn, "select * from mashData " + "where symbol='SH600000' order by dataTime desc");
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<HashMap<String, Object>> list = null;
        try {
            list = QueryTool.resultSetToList(resultSet);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < days; i++) {
            HashMap<String, Object> map = list.get(i);
            Mash mash=new Mash();
            mash.setId((int)(long)map.get("id"));
            mash.setSymbol((String)map.get("symbol"));
            mash.setMa5Price((double)map.get("ma5_price"));
            mash.setMa5Volume((double)map.get("ma5_volume"));
            mash.setMa10Price((double)map.get("ma10_price"));
            mash.setMa10Volume((double)map.get("ma10_volume"));
            mash.setMa20Price((double)map.get("ma20_price"));
            mash.setMa20Volume((double)map.get("ma20_volume"));
            mash.setDiff((double)map.get("diff"));
            mash.setDea((double)map.get("dea"));
            mash.setMacd((double)map.get("macd"));
            mash.setK((double)map.get("k"));
            mash.setD((double)map.get("d"));
            mash.setJ((double)map.get("j"));
            mash.setRsi1((double)map.get("rsi1"));
            mash.setRsi2((double)map.get("rsi2"));
            mash.setRsi3((double)map.get("rsi3"));
            mash.setDate(map.get("dataTime").toString());
            MashList.add(mash);
        }
        conn.close();
        return MashList;
    }

    /**根据Ma5来预测*/
    public void predictByMa5Price()
    {
        ChangePredict test=new ChangePredict();
        PricePredict pricetest=new PricePredict();

        List<Mash> Mashs=test.train("2016");
        List<DayK> dayKs=pricetest.train("2016");
        List<Integer> changeList=new ArrayList<>();
        for(int i=0;i<dayKs.size();i++)
        {
            double change=dayKs.get(i).getClose()-dayKs.get(i).getOpen();
            if(change>0)//涨
            {
                changeList.add(1);
            }
            else if(change<0)//跌
            {
                changeList.add(-1);
            }
            else//平
            {
                changeList.add(0);
            }
        }

        svm_node[][] datas =new svm_node[Mashs.size()-days][days];//训练集的向量表//
        double[] labelss = new double[Mashs.size()-days];//对应的labels
        for(int i=days-1;i<Mashs.size()-1;i++)
        {
            svm_node[] svm_nodes=new svm_node[days];//
            for(int j=0;j<days;j++)
            {
                svm_node pa0 = new svm_node();
                pa0.index = 0;
                pa0.value = Mashs.get(i-j).getMa5Price();
                svm_nodes[0+j]=pa0;
            }

            datas[i-days+1]=svm_nodes;
            labelss[i - days + 1] = changeList.get(i+1);
        }


        //定义svm_problem对象
        svm_problem problem = new svm_problem();
        problem.l = Mashs.size()-days; //向量个数
        problem.x = datas; //训练集向量表
        problem.y = labelss; //对应的labels数组

        //定义svm_parameter对象
        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.00001;
        param.C = 1;

        //训练SVM分类模型
        System.out.println(svm.svm_check_parameter(problem, param)); //如果参数没有问题，则svm.svm_check_parameter()函数返回null,否则返回error描述。
        svm_model model = svm.svm_train(problem, param); //svm.svm_train()训练出SVM分类模型

        //定义测试数据点c
        List<Mash> Mashs2=test.predict();
        svm_node[] svm_nodes=new svm_node[days];//
        for(int j=0;j<days;j++)
        {
            svm_node pa0 = new svm_node();
            pa0.index = 0;
            pa0.value = Mashs2.get(j).getMa5Price();

            svm_nodes[0+j]=pa0;
        }
        //预测测试数据的labels
        int index=(int)svm.svm_predict(model, svm_nodes);

        System.out.println(index);
    }
    /**根据Ma10来预测*/
    public void predictByMa10Price()
    {
        ChangePredict test=new ChangePredict();
        PricePredict pricetest=new PricePredict();

        List<Mash> Mashs=test.train("2016");
        List<DayK> dayKs=pricetest.train("2016");
        List<Integer> changeList=new ArrayList<>();
        for(int i=0;i<dayKs.size();i++)
        {
            double change=dayKs.get(i).getClose()-dayKs.get(i).getOpen();
            if(change>0)//涨
            {
                changeList.add(1);
            }
            else if(change<0)//跌
            {
                changeList.add(-1);
            }
            else//平
            {
                changeList.add(0);
            }
        }

        svm_node[][] datas =new svm_node[Mashs.size()-days][days];//训练集的向量表//
        double[] labelss = new double[Mashs.size()-days];//对应的labels
        for(int i=days-1;i<Mashs.size()-1;i++)
        {
            svm_node[] svm_nodes=new svm_node[days];//
            for(int j=0;j<days;j++)
            {
                svm_node pa0 = new svm_node();
                pa0.index = 0;
                pa0.value = Mashs.get(i-j).getMa10Price();
                svm_nodes[0+j]=pa0;
            }

            datas[i-days+1]=svm_nodes;
            labelss[i - days + 1] = changeList.get(i+1);
        }


        //定义svm_problem对象
        svm_problem problem = new svm_problem();
        problem.l = Mashs.size()-days; //向量个数
        problem.x = datas; //训练集向量表
        problem.y = labelss; //对应的labels数组

        //定义svm_parameter对象
        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.00001;
        param.C = 1;

        //训练SVM分类模型
        System.out.println(svm.svm_check_parameter(problem, param)); //如果参数没有问题，则svm.svm_check_parameter()函数返回null,否则返回error描述。
        svm_model model = svm.svm_train(problem, param); //svm.svm_train()训练出SVM分类模型

        //定义测试数据点c
        List<Mash> Mashs2=test.predict();
        svm_node[] svm_nodes=new svm_node[days];//
        for(int j=0;j<days;j++)
        {
            svm_node pa0 = new svm_node();
            pa0.index = 0;
            pa0.value = Mashs2.get(j).getMa10Price();

            svm_nodes[0+j]=pa0;
        }
        //预测测试数据的labels
        int index=(int)svm.svm_predict(model, svm_nodes);

        System.out.println(index);
    }
    /**根据Ma20来预测*/
    public void predictByMa20Price()
    {
        ChangePredict test=new ChangePredict();
        PricePredict pricetest=new PricePredict();

        List<Mash> Mashs=test.train("2016");
        List<DayK> dayKs=pricetest.train("2016");
        List<Integer> changeList=new ArrayList<>();
        for(int i=0;i<dayKs.size();i++)
        {
            double change=dayKs.get(i).getClose()-dayKs.get(i).getOpen();
            if(change>0)//涨
            {
                changeList.add(1);
            }
            else if(change<0)//跌
            {
                changeList.add(-1);
            }
            else//平
            {
                changeList.add(0);
            }
        }

        svm_node[][] datas =new svm_node[Mashs.size()-days][days];//训练集的向量表//
        double[] labelss = new double[Mashs.size()-days];//对应的labels
        for(int i=days-1;i<Mashs.size()-1;i++)
        {
            svm_node[] svm_nodes=new svm_node[days];//
            for(int j=0;j<days;j++)
            {
                svm_node pa0 = new svm_node();
                pa0.index = 0;
                pa0.value = Mashs.get(i-j).getMa20Price();
                svm_nodes[0+j]=pa0;
            }

            datas[i-days+1]=svm_nodes;
            labelss[i - days + 1] = changeList.get(i+1);
        }


        //定义svm_problem对象
        svm_problem problem = new svm_problem();
        problem.l = Mashs.size()-days; //向量个数
        problem.x = datas; //训练集向量表
        problem.y = labelss; //对应的labels数组

        //定义svm_parameter对象
        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.00001;
        param.C = 1;

        //训练SVM分类模型
        System.out.println(svm.svm_check_parameter(problem, param)); //如果参数没有问题，则svm.svm_check_parameter()函数返回null,否则返回error描述。
        svm_model model = svm.svm_train(problem, param); //svm.svm_train()训练出SVM分类模型

        //定义测试数据点c
        List<Mash> Mashs2=test.predict();
        svm_node[] svm_nodes=new svm_node[days];//
        for(int j=0;j<days;j++)
        {
            svm_node pa0 = new svm_node();
            pa0.index = 0;
            pa0.value = Mashs2.get(j).getMa20Price();

            svm_nodes[0+j]=pa0;
        }
        //预测测试数据的labels
        int index=(int)svm.svm_predict(model, svm_nodes);

        System.out.println(index);
    }
    /**根据Ma5、Ma10、Ma20来预测*/
    public void predictByMa51020Price()
    {
        ChangePredict test = new ChangePredict();
        PricePredict pricetest = new PricePredict();

        List<Mash> Mashs = test.train("2016");
        List<DayK> dayKs = pricetest.train("2016");
        List<Integer> changeList = new ArrayList<>();
        for (int i = 0; i < dayKs.size(); i++) {
            double change = dayKs.get(i).getClose() - dayKs.get(i).getOpen();
            if (change > 0)//涨
            {
                changeList.add(1);
            } else if (change < 0)//跌
            {
                changeList.add(-1);
            } else//平
            {
                changeList.add(0);
            }
        }

        svm_node[][] datas = new svm_node[Mashs.size() - days][3 * days];//训练集的向量表//
        double[] labelss = new double[Mashs.size() - days];//对应的labels
        for (int i = days - 1; i < Mashs.size() - 1; i++) {
            svm_node[] svm_nodes = new svm_node[3 * days];//
            for (int j = 0; j < days; j++) {
                svm_node pa0 = new svm_node();
                pa0.index = 0;
                pa0.value = Mashs.get(i - j).getMa5Price();
                svm_node pa1 = new svm_node();
                pa1.index = 1;
                pa1.value = Mashs.get(i - j).getMa10Price();
                svm_node pa2 = new svm_node();
                pa2.index = 2;
                pa2.value = Mashs.get(i - j).getMa20Price();
                svm_nodes[0 + 3 * j] = pa0;
                svm_nodes[1 + 3 * j] = pa1;
                svm_nodes[2 + 3 * j] = pa2;
            }
            datas[i - days + 1] = svm_nodes;
            labelss[i - days + 1] = changeList.get(i + 1);
        }


        //定义svm_problem对象
        svm_problem problem = new svm_problem();
        problem.l = Mashs.size() - days; //向量个数
        problem.x = datas; //训练集向量表
        problem.y = labelss; //对应的labels数组

        //定义svm_parameter对象
        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.00001;
        param.C = 1;

        //训练SVM分类模型
        System.out.println(svm.svm_check_parameter(problem, param)); //如果参数没有问题，则svm.svm_check_parameter()函数返回null,否则返回error描述。
        svm_model model = svm.svm_train(problem, param); //svm.svm_train()训练出SVM分类模型

        //定义测试数据点c
        List<Mash> Mashs2 = test.predict();
        svm_node[] svm_nodes = new svm_node[3 * days];//
        for (int j = 0; j < days; j++) {
            svm_node pa0 = new svm_node();
            pa0.index = 0;
            pa0.value = Mashs2.get(j).getMa5Price();
            svm_node pa1 = new svm_node();
            pa1.index = 1;
            pa1.value = Mashs2.get(j).getMa10Price();
            svm_node pa2 = new svm_node();
            pa2.index = 0;
            pa2.value = Mashs2.get(j).getMa20Price();
            svm_nodes[0 + 3*j] = pa0;
            svm_nodes[1 + 3*j] = pa1;
            svm_nodes[2 + 3*j] = pa2;
        }
        //预测测试数据的labels
        int index = (int) svm.svm_predict(model, svm_nodes);

        System.out.println(index);
    }

    /**根据Macd、Diff、Dea来预测*/
    public void predictByMacd()
    {
        ChangePredict test = new ChangePredict();
        PricePredict pricetest = new PricePredict();

        List<Mash> Mashs = test.train("2016");
        List<DayK> dayKs = pricetest.train("2016");
        List<Integer> changeList = new ArrayList<>();
        for (int i = 0; i < dayKs.size(); i++) {
            double change = dayKs.get(i).getClose() - dayKs.get(i).getOpen();
            if (change > 0)//涨
            {
                changeList.add(1);
            } else if (change < 0)//跌
            {
                changeList.add(-1);
            } else//平
            {
                changeList.add(0);
            }
        }

        svm_node[][] datas = new svm_node[Mashs.size() - days][3 * days];//训练集的向量表//
        double[] labelss = new double[Mashs.size() - days];//对应的labels
        for (int i = days - 1; i < Mashs.size() - 1; i++) {
            svm_node[] svm_nodes = new svm_node[3 * days];//
            for (int j = 0; j < days; j++) {
                svm_node pa0 = new svm_node();
                pa0.index = 0;
                pa0.value = Mashs.get(i - j).getMacd();
                svm_node pa1 = new svm_node();
                pa1.index = 1;
                pa1.value = Mashs.get(i - j).getDiff();
                svm_node pa2 = new svm_node();
                pa2.index = 2;
                pa2.value = Mashs.get(i - j).getDea();
                svm_nodes[0 + 3 * j] = pa0;
                svm_nodes[1 + 3 * j] = pa1;
                svm_nodes[2 + 3 * j] = pa2;
            }
            datas[i - days + 1] = svm_nodes;
            labelss[i - days + 1] = changeList.get(i + 1);
        }


        //定义svm_problem对象
        svm_problem problem = new svm_problem();
        problem.l = Mashs.size() - days; //向量个数
        problem.x = datas; //训练集向量表
        problem.y = labelss; //对应的labels数组

        //定义svm_parameter对象
        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.00001;
        param.C = 1;

        //训练SVM分类模型
        System.out.println(svm.svm_check_parameter(problem, param)); //如果参数没有问题，则svm.svm_check_parameter()函数返回null,否则返回error描述。
        svm_model model = svm.svm_train(problem, param); //svm.svm_train()训练出SVM分类模型

        //定义测试数据点c
        List<Mash> Mashs2 = test.predict();
        svm_node[] svm_nodes = new svm_node[3 * days];//
        for (int j = 0; j < days; j++) {
            svm_node pa0 = new svm_node();
            pa0.index = 0;
            pa0.value = Mashs2.get(j).getMacd();
            svm_node pa1 = new svm_node();
            pa1.index = 1;
            pa1.value = Mashs2.get(j).getDiff();
            svm_node pa2 = new svm_node();
            pa2.index = 0;
            pa2.value = Mashs2.get(j).getDea();
            svm_nodes[0 + 3*j] = pa0;
            svm_nodes[1 + 3*j] = pa1;
            svm_nodes[2 + 3*j] = pa2;
        }
        //预测测试数据的labels
        int index = (int) svm.svm_predict(model, svm_nodes);

        System.out.println(index);
    }

    /**根据K、D、J来预测*/
    public void predictByKDJ()
    {
        ChangePredict test = new ChangePredict();
        PricePredict pricetest = new PricePredict();

        List<Mash> Mashs = test.train("2016");
        List<DayK> dayKs = pricetest.train("2016");
        List<Integer> changeList = new ArrayList<>();
        for (int i = 0; i < dayKs.size(); i++) {
            double change = dayKs.get(i).getClose() - dayKs.get(i).getOpen();
            if (change > 0)//涨
            {
                changeList.add(1);
            } else if (change < 0)//跌
            {
                changeList.add(-1);
            } else//平
            {
                changeList.add(0);
            }
        }

        svm_node[][] datas = new svm_node[Mashs.size() - days][3 * days];//训练集的向量表//
        double[] labelss = new double[Mashs.size() - days];//对应的labels
        for (int i = days - 1; i < Mashs.size() - 1; i++) {
            svm_node[] svm_nodes = new svm_node[3 * days];//
            for (int j = 0; j < days; j++) {
                svm_node pa0 = new svm_node();
                pa0.index = 0;
                pa0.value = Mashs.get(i - j).getK();
                svm_node pa1 = new svm_node();
                pa1.index = 1;
                pa1.value = Mashs.get(i - j).getD();
                svm_node pa2 = new svm_node();
                pa2.index = 2;
                pa2.value = Mashs.get(i - j).getJ();
                svm_nodes[0 + 3 * j] = pa0;
                svm_nodes[1 + 3 * j] = pa1;
                svm_nodes[2 + 3 * j] = pa2;
            }
            datas[i - days + 1] = svm_nodes;
            labelss[i - days + 1] = changeList.get(i + 1);
        }


        //定义svm_problem对象
        svm_problem problem = new svm_problem();
        problem.l = Mashs.size() - days; //向量个数
        problem.x = datas; //训练集向量表
        problem.y = labelss; //对应的labels数组

        //定义svm_parameter对象
        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.00001;
        param.C = 1;

        //训练SVM分类模型
        System.out.println(svm.svm_check_parameter(problem, param)); //如果参数没有问题，则svm.svm_check_parameter()函数返回null,否则返回error描述。
        svm_model model = svm.svm_train(problem, param); //svm.svm_train()训练出SVM分类模型

        //定义测试数据点c
        List<Mash> Mashs2 = test.predict();
        svm_node[] svm_nodes = new svm_node[3 * days];//
        for (int j = 0; j < days; j++) {
            svm_node pa0 = new svm_node();
            pa0.index = 0;
            pa0.value = Mashs2.get(j).getK();
            svm_node pa1 = new svm_node();
            pa1.index = 1;
            pa1.value = Mashs2.get(j).getD();
            svm_node pa2 = new svm_node();
            pa2.index = 0;
            pa2.value = Mashs2.get(j).getJ();
            svm_nodes[0 + 3*j] = pa0;
            svm_nodes[1 + 3*j] = pa1;
            svm_nodes[2 + 3*j] = pa2;
        }
        //预测测试数据的labels
        int index = (int) svm.svm_predict(model, svm_nodes);

        System.out.println(index);
    }

    /**根据Rsi1、Rsi2、Rsi3来预测*/
    public void predictByRsi()
    {
        ChangePredict test = new ChangePredict();
        PricePredict pricetest = new PricePredict();

        List<Mash> Mashs = test.train("2016");
        List<DayK> dayKs = pricetest.train("2016");
        List<Integer> changeList = new ArrayList<>();
        for (int i = 0; i < dayKs.size(); i++) {
            double change = dayKs.get(i).getClose() - dayKs.get(i).getOpen();
            if (change > 0)//涨
            {
                changeList.add(1);
            } else if (change < 0)//跌
            {
                changeList.add(-1);
            } else//平
            {
                changeList.add(0);
            }
        }

        svm_node[][] datas = new svm_node[Mashs.size() - days][3 * days];//训练集的向量表//
        double[] labelss = new double[Mashs.size() - days];//对应的labels
        for (int i = days - 1; i < Mashs.size() - 1; i++) {
            svm_node[] svm_nodes = new svm_node[3 * days];//
            for (int j = 0; j < days; j++) {
                svm_node pa0 = new svm_node();
                pa0.index = 0;
                pa0.value = Mashs.get(i - j).getRsi1();
                svm_node pa1 = new svm_node();
                pa1.index = 1;
                pa1.value = Mashs.get(i - j).getRsi2();
                svm_node pa2 = new svm_node();
                pa2.index = 2;
                pa2.value = Mashs.get(i - j).getRsi3();
                svm_nodes[0 + 3 * j] = pa0;
                svm_nodes[1 + 3 * j] = pa1;
                svm_nodes[2 + 3 * j] = pa2;
            }
            datas[i - days + 1] = svm_nodes;
            labelss[i - days + 1] = changeList.get(i + 1);
        }


        //定义svm_problem对象
        svm_problem problem = new svm_problem();
        problem.l = Mashs.size() - days; //向量个数
        problem.x = datas; //训练集向量表
        problem.y = labelss; //对应的labels数组

        //定义svm_parameter对象
        svm_parameter param = new svm_parameter();
        param.svm_type = svm_parameter.C_SVC;
        param.kernel_type = svm_parameter.LINEAR;
        param.cache_size = 100;
        param.eps = 0.00001;
        param.C = 1;

        //训练SVM分类模型
        System.out.println(svm.svm_check_parameter(problem, param)); //如果参数没有问题，则svm.svm_check_parameter()函数返回null,否则返回error描述。
        svm_model model = svm.svm_train(problem, param); //svm.svm_train()训练出SVM分类模型

        //定义测试数据点c
        List<Mash> Mashs2 = test.predict();
        svm_node[] svm_nodes = new svm_node[3 * days];//
        for (int j = 0; j < days; j++) {
            svm_node pa0 = new svm_node();
            pa0.index = 0;
            pa0.value = Mashs2.get(j).getRsi1();
            svm_node pa1 = new svm_node();
            pa1.index = 1;
            pa1.value = Mashs2.get(j).getRsi2();
            svm_node pa2 = new svm_node();
            pa2.index = 0;
            pa2.value = Mashs2.get(j).getRsi3();
            svm_nodes[0 + 3*j] = pa0;
            svm_nodes[1 + 3*j] = pa1;
            svm_nodes[2 + 3*j] = pa2;
        }
        //预测测试数据的labels
        int index = (int) svm.svm_predict(model, svm_nodes);

        System.out.println(index);
    }

    public static void main(String[] args)
    {
        ChangePredict test=new ChangePredict();
        test.predictByKDJ();
    }
}
