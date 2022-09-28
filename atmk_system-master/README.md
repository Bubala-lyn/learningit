# ATMK研究平台

## 数据
链接: https://pan.baidu.com/s/1BE4yLZq2CBbicmblXQgMdw?pwd=w4ii 提取码: w4ii

+ sql 文件为 Web 服务的 MySQL 数据库文件
+ 其余为词向量和公式向量等，需复制到当前项目的 `file_data` 目录下

## Web服务

### 拉起服务

```bash
python manage.py runserver 9000 # 后台服务
cd frontend
npm run serve # 前端开发服务
```

### 检测对模型文件的修改，并且把修改的部分储存为一次迁移
```bash
python manage.py makemigrations math_questions
```
### 执行数据库迁移并同步管理数据库结构
```bash
python manage.py migrate
```

## 数据清洗

详见【ATMK研究平台-数据清洗】页面

保存的原始数据格式：

数据文件：`file_data\math_questions_content.pkl`

```json
    [
        {
            "id": 1,
            "text":"题目HEL_45293_WLDOR_1_OL文本",
            "math_text": "题目文本 with formulas",
            "char_formula_list": [],
            "word_list": [],
            "word_formula_list": [],
            "label_list": [],
            "formulas": {
                "HEL_45293_WLDOR_1_OL": "mathML"
            }
        }
    ]

```

## 公式学习

```bash
cd formula_embedding
python train_model.py
```

公式学习时待处理数据的数据格式：

```json
    [
        {
            "content": "<math></math>",
            "key": "HEL_45293_WLDOR_1_OL"
        }
    ]

```

## 数据预处理

可视化操作见【ATMK研究平台-模型训练】页面，代码实现见： `MathByte\preprocess.py`

数据文件：`file_data\math_data.h5` 和 `file_data\vocab_label.pkl` 和 `file_data\embeddings.pkl`

+ `file_data\math_data.h5` 存储的是字典
```Python
{
    'train_X': [[1,12,233,10002,0.......]], # 一组序列编码，每个item是题目序列，序列长度100
    'train_Y': [[1,1,0,1,0,0,........]], # 一组多热编码，每个item是题目的知识点集，标签总数？
    'vaild_X': [[]], 
    'valid_Y': [[]], 
    'test_X': [[]],
    'test_Y': [[]]
}
```

+ `file_data\vocab_label.pkl` 存储的是元祖`(word2index, label2index)`
+ `file_data\embeddings.pkl` 存储的是 narray 二维数组

## 分类模型训练

```bash
cd MathByte
python classification.py -h # 查看命令参数
python classification.py
python classification.py --use_lcm=True # 使用 label confusion model
python classification.py --use_att=True # 使用 label attention
python classification.py --use_lcm=True --use_att=True # 使用 ls & la
python classification.py --config=config/config_a1.yml # 指定配置文件
```

## 鸣谢
+ [TangentCFT](https://github.com/BehroozMansouri/TangentCFT)
+ [Embedding/Chinese-Word-Vectors](https://github.com/Embedding/Chinese-Word-Vectors)
+ [brightmart/text_classification](https://github.com/brightmart/text_classification)
+ [EMNLP2019LSAN/LSAN](https://github.com/EMNLP2019LSAN/LSAN/)