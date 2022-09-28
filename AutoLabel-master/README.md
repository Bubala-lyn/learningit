# AutoLabel

## 本地开发
### JAVA
+ JDK 1.8
+ IDE eclipse
+ maven 项目
### mysql
+ 模型训练需连接数据库
+ 数据集存储于数据库：[testbank7](testbank7.sql)，更多信息见 `autolabel.properties`

## 注意事项
+ <span style="color:red">清华分词工具jar包是从外部引入，该jar已在本项目中，但需把 `pom.xml` 32行的路径替换成该jar包的真实绝对路径</span>

## mysql数据库初次连接
```bash
mysql -u root -p
Enter password: # 不输入直接enter
mysql> use mysql;
Database changed
mysql> alter user 'root'@'localhost' identified with mysql_native_password by '123456'; # 改密码
Query OK, 0 rows affected (0.00 sec)
mysql> flush privileges;
Query OK, 0 rows affected (0.00 sec)
```

## sql导入指南

```bash
mysql -u root -p
Enter password: ******
mysql> CREATE DATABASE testbank7;
mysql> exit
mysql -u root -p testbank7 < testbank7.sql
mysql -u root -p testbank7
Enter password: ******
```

## 执行程序
### 模型训练
调用`KFoldKnowledgeEngine`类，具体可参考`AdaBoostClientTest2.java`测试用例
### 自动标记
调用`KFoldKnowledgeEngine`类，具体可参考`PredictClientTest.java`测试用例
## 实验流程
  + 一份原始数据：分成 k + 1 份
  + 其中一份固定作为测试集
  + 其余 k 份作为训练集进行k折验证实验
  + 每折：
    + 把1/k 的训练集作为验证集；
    + 在训练集上：
      + 计算概率；
      + adaboost调整概率；
      + 保存概率；
    + 在验证集上计算模型性能并保存；
    + 计算k次实验的平均精确度、召回率、F1分数；
    + 在全部的训练集上训练，并在测试集上测试，保存概率和性能数据；
    + 选出合适的模型

## 模块说明
**Train.segment**
+ 功能：分词
+ 数据：题目内容分词、题目选项分词、知识点的先验概率
+ 流程：
  + 分词工具加载词典等
  + 加载本地关键词
  + 清缓存（清除原本加载的数据，题目分词结果、先验概率）
  + 初始化缓存（加载停用词、特殊符号、关键词）
  + 对题目内容、题目选项进行分词，结果分别保存在对应的分词结果表中

**Train.updateAndCompute**
+ 功能：计算分词的条件概率
+ 数据：分词权重表（tf,idf等）、训练数据特征集、条件概率
+ 流程：
  + 清缓存（把计算结果先清空）
  + count
  + 计算条件概率（分词在知识点下的概率）


## 数据库表说明
### 原始数据说明
+ t_question_content_mathematics_copy1
题目表
+ t_question_item_mathematics_copy1
题目选项表
+ t_knowledge_question_mathematics_copy1
知识点题目对照表
+ t_knowledge_copy1
知识点
### 训练结果中间计算结果表
+ t_question_content_mathematics_seg
说明：试题题干分词列表
+ t_question_item_mathematics_seg
说明：试题选项分词列表
+ t_knowledge_mathematics_probability
说明：知识点概率
+ t_knowledge_mathematics_word_probability
说明：分词|知识点条件概率（包含AdaBoost概率）
+ t_question_content_mathematics_count
说明：【训练用】分词的各参数计算结果表
+ t_question_mathematics_word
说明：【训练用】题目的分词表示表

### 验证
+ t_question_content_mathematics_test
说明：验证集试题题干
+ t_question_item_mathematics_test
说明：验证集试题选项
+ t_question_mathematics_word_test
说明： 验证集题目的分词表示表

## 2021/6/27 更新

+ 当前引入两种模型提优的方式：解析公式和扩展文本

公式以及含义对照表：`src\main\resources\latexSymbol.json`

是否解析公式的开关位于：
```Java
if (qName.trim().toLowerCase().equals("latex")) {
      isLatex = true; // NOTE 这里标记为 false 即不解析公式
}

```
如何扩展文本
```Java
// 不包含答案和解析的修改
concat('<document>', IFNULL(content,''),IFNULL(answer,''),IFNULL(analyse,''), '</document>') as content
// 修改为
content

```
