# DraFinance_svm

利用Java版LibSVM

SVM预测下个交易日的开盘价和涨跌情况

开盘价预测使用日K数据的开盘、收盘、最高、最低

涨跌情况使用各个指标

注1：
仅预测了下个交易日
训练集都采用2016年数据库中有的全部数据
用于预测的时间取7天
代码中原生数据库访问
预测结果使用控制台打印的方式呈现
主要的功能类在predict包中

注2：
需要将lib包中的加到工程的library中

注3：
有时间的话@馨中完成到Web项目的迁移
DayK.java、Mash.java是从web项目拷贝来的
svm_predict、svm_train是从libSVM中拷贝来的，svm_scale未使用

注4：
有需要再修改和添加


于凡 2017/07/09
