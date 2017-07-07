package helper;

import java.io.IOException;

import libsvm.*;

/**JAVA train code for LibSVM
 * @author yangliu
 * @blog http://blog.csdn.net/yangliuy 
 * @mail yangliuyx@gmail.com 
 */

public class LibSVMTest {

    public static void main(String[] args) throws IOException {
        //定义训练集点a{10.0, 10.0} 和 点b{-10.0, -10.0}，对应lable为{1.0, -1.0}
        svm_node pa0 = new svm_node();
        pa0.index = 0;
        pa0.value = 25.5;
        svm_node pa1 = new svm_node();
        pa1.index = 1;
        pa1.value = 26.78;
        svm_node pa2 = new svm_node();
        pa1.index = 2;
        pa1.value = 26.73;
        svm_node pa3 = new svm_node();
        pa1.index = 3;
        pa1.value = 25.8;

        svm_node pb0 = new svm_node();
        pb0.index = 0;
        pb0.value = 26.61;
        svm_node pb1 = new svm_node();
        pb1.index = 1;
        pb1.value = 27.27;
        svm_node pb2 = new svm_node();
        pb1.index = 2;
        pb1.value = 26.85;
        svm_node pb3 = new svm_node();
        pb1.index = 3;
        pb1.value = 26.73;

        svm_node pc0 = new svm_node();
        pc0.index = 0;
        pc0.value = 26.3;
        svm_node pc1 = new svm_node();
        pc1.index = 1;
        pc1.value = 27.01;
        svm_node pc2 = new svm_node();
        pc1.index = 2;
        pc1.value = 26.8;
        svm_node pc3 = new svm_node();
        pc1.index = 3;
        pc1.value = 26.72;
        
        svm_node[] pa = {pa0, pa1,pa2,pa3}; //点a
        svm_node[] pb = {pb0, pb1,pb2,pb3}; //点b
        svm_node[] pc = {pc0, pc1,pc2,pc3}; //点c
        svm_node[][] datas = {pa, pb,pc}; //训练集的向量表
        double[] lables = {26.85,26.8,26.71}; //a,b 对应的lable

        //定义svm_problem对象
        svm_problem problem = new svm_problem();
        problem.l = 3; //向量个数
        problem.x = datas; //训练集向量表
        problem.y = lables; //对应的lable数组

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
        svm_node pz0 = new svm_node();
        pz0.index = 0;
        pz0.value = 26.52;
        svm_node pz1 = new svm_node();
        pz1.index = 1;
        pz1.value = 27.29;
        svm_node pz2 = new svm_node();
        pz1.index = 2;
        pz1.value = 26.71;
        svm_node pz3 = new svm_node();
        pz1.index = 3;
        pz1.value = 27.05;
        svm_node[] pz = {pz0, pz1,pz2,pz3};

        //预测测试数据的lable
        System.out.println(svm.svm_predict(model, pz));


//        String filepath = "";
//        //param:   -v:设置10折交叉验证
//        //param:   train1.txt文件是训练数据
//        //param:   trainfile\\modle_r.txt是存储训练出来的模型的文件
//        String[] arg = {"-v","10",filepath+"breast-cancer","model_r.txt"};
//        String[] parg = {filepath+"breast-cancer","model_r.txt",
//                "out_r.txt"};
//        System.out.println("----------------SVM运行开始-----------------");
//
////      String[] arg = {"-s","0","-c","5","-t","2","-g","0.5","-e","0.1","model.txt"};
////      svm_train.main(arg);
//
//        svm_train.main(arg);//训练
//        svm_predict.main(parg);//预测或分类
    }

}  