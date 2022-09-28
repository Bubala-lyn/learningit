# coding: UTF-8
import os
import numpy as np
import argparse
import logging
from datetime import datetime
import pytz
from sklearn.model_selection import KFold, train_test_split

from models import trainer
import utils

logging.basicConfig(
    level=logging.INFO,
    # filename='train.log',
    # filemode='a',
    format='%(asctime)s : %(levelname)s : %(message)s',
)

parser = argparse.ArgumentParser(description='ATMK')
parser.add_argument('--use_att', default=False, type=bool,
                    help='True for use label attention')
parser.add_argument('--use_lcm', default=False, type=bool,
                    help='True for use label confusion model')
parser.add_argument('--config', default='config/config_waa1.yml', type=str,
                    help='config file')
args = parser.parse_args()

if __name__ == '__main__':
    # 加载配置文件
    logging.info("Loading config...")
    config = utils.read_config(args.config)
    logging.info(config)
    # 加载数据
    logging.info("Loading data...")
    word2index, label2index, X, y = utils.load_data(
        config.cache_file_h5py, config.cache_file_pickle)
    config.vocab_size = len(word2index)
    config.num_classes = len(label2index)
    # 加载预训练的向量
    logging.info("Loading embeddings...")
    embeddings_2dlist = utils.load_embed_data(config.embeddings)
    label_emb_2dlist = None
    if config.get('label_embeddings', None):
        label_emb_2dlist = utils.load_embed_data(config.label_embeddings)
    # 当前模型名称
    model_name = "b"
    if args.use_att & args.use_lcm:
        model_name = "labs"
    elif args.use_att:
        model_name = "lab"
    elif args.use_lcm:
        model_name = "lbs"
    logging.info("model name %s" % model_name)
    # ========== model training: ==========
    X = np.array(X)
    y = np.array(y)
    # shuffle, split,
    # 确保每次随机出来的数据是一样的
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.25, random_state=63)
    print("TOTAL:", len(X),
          "TRAIN:", X_train, len(X_train),
          "TEST:", X_test, len(X_test))
    file_id = '%s-%s-%s' % (utils.randomword(6), model_name, datetime.now(pytz.timezone('Asia/Shanghai')
                                                                          ).strftime("%m%d-%H%M%S"))
    log_dir = os.path.join('logs', file_id)
    np.random.seed(1)  # 这样保证了每次试验的seed一致
    '''
    初始化模拟标签数据（L_train,L_test）
    shape=(None,num_classes)
    [[  0   1   2 ... 424 425 426]
        [  0   1   2 ... 424 425 426]
        [  0   1   2 ... 424 425 426]
        ...
        [  0   1   2 ... 424 425 426]
        [  0   1   2 ... 424 425 426]
        [  0   1   2 ... 424 425 426]]
    '''
    L_train = np.array([np.array(range(config.num_classes))
                        for i in range(len(X_train))])
    L_test = np.array([np.array(range(config.num_classes))
                       for i in range(len(X_test))])
    logging.info('=====Start=====')
    labs_model = trainer.LABSModel(
        config, embeddings_2dlist, label_emb_matrix=label_emb_2dlist, use_att=args.use_att, use_lcm=args.use_lcm, log_dir=log_dir)
    labs_model.train(X_train, y_train, L_train)
    labs_model.validate(X_test, y_test, L_test)
    logging.info('=======End=======')
    # 模型训练完毕后在测试集上最终评估
