dict_build
==========

build dict from large chinese text using unsupervised method，algorithm：http://www.matrix67.com/blog/archives/5044


###成词条件
1. 互信息
2. 左右熵
3. 位置成词概率
4. ngram 频率

###运行方法
见Main.java，输入参数为原始文本文件路径。
输出为words_sort.data，抽取出来的词，可根据阈值进行调整。


###TODO
1. 单机支持100G+数据集的抽取。
2. 整理代码
