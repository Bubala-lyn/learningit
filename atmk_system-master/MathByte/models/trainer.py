import numpy as np
import os
import keras
from keras.callbacks import TensorBoard, EarlyStopping, ModelCheckpoint, CSVLogger
from keras.models import load_model
import keras.backend as K

from .lstm import Classifier
from .lcm import LabelConfusionModel
from .evaluation_metrics import basic_metrics, lcm_metrics


class LABSModel:

    def __init__(self, config, text_embedding_matrix=None, label_emb_matrix=None, use_att=False, use_lcm=False, log_dir=None):
        self.epochs = config.epochs
        self.alpha = config.alpha
        self.num_classes = config.num_classes
        self.batch_size = config.batch_size
        self.use_att = use_att
        self.use_lcm = use_lcm
        self.model_filepath = os.path.join(
            log_dir, "model", self.__get_saved_model_name())

        self.basic_model, hid, label_emb = Classifier.build(
            config, text_embedding_matrix, use_att, label_emb_matrix, basic_metrics())
        es_monitor = "val_loss"
        mc_monitor = "val_precision_1k"
        patience = 2
        if (use_att == False) & (use_lcm == False):
            patience = 20  # basic 模型做较大设置
        print(patience, "patience")

        if use_lcm:
            loss, metrics = lcm_metrics(self.num_classes, self.alpha)
            self.model = LabelConfusionModel.build(
                config, self.basic_model, hid, label_emb, loss, metrics)
            mc_monitor = "val_lcm_precision_1k"
        # 设置训练过程中的回调函数
        tb = TensorBoard(log_dir=os.path.join(log_dir, "fit"))
        # 设置 early stop
        es = EarlyStopping(monitor=es_monitor, mode='min',
                           verbose=1, patience=patience, min_delta=0.0001)
        # 保存 val_loss 最小时的model
        mc = ModelCheckpoint(self.model_filepath, monitor=es_monitor,
                             mode='min', verbose=1, save_best_only=True)
        # 保存训练过程数据到csv文件
        logger = CSVLogger(os.path.join(log_dir, "training.csv"))
        self.callbacks = [tb, es, mc, logger]

    def train(self, X_train, y_train, L_train):
        model = self.model if self.use_lcm else self.basic_model
        model.fit([X_train, L_train], y_train,
                  batch_size=self.batch_size, verbose=1, epochs=self.epochs, validation_split=0.2, callbacks=self.callbacks)

    def validate(self, X_test, y_test, L_test):
        loss, metrics = lcm_metrics(self.num_classes, self.alpha)
        b_metrics = basic_metrics()
        # load the saved model
        saved_model = load_model(self.model_filepath, custom_objects={
            "K": K,
            "precision_1k": b_metrics[0],
            "precision_2k": b_metrics[1],
            "precision_3k": b_metrics[2],
            "precision_5k": b_metrics[3],
            "recall_1k": b_metrics[4],
            "recall_2k": b_metrics[5],
            "recall_3k": b_metrics[6],
            "recall_5k": b_metrics[7],
            "F1_1k": b_metrics[8],
            "F1_2k": b_metrics[9],
            "F1_3k": b_metrics[10],
            "F1_5k": b_metrics[11],
            "lcm_loss": loss,
            "lcm_precision_1k": metrics[0],
            "lcm_precision_2k": metrics[1],
            "lcm_precision_3k": metrics[2],
            "lcm_precision_5k": metrics[3],
            "lcm_recall_1k": metrics[4],
            "lcm_recall_2k": metrics[5],
            "lcm_recall_3k": metrics[6],
            "lcm_recall_5k": metrics[7],
            "lcm_f1_1k": metrics[8],
            "lcm_f1_2k": metrics[9],
            "lcm_f1_3k": metrics[10],
            "lcm_f1_5k": metrics[11],
        })
        # evaluate the model
        result = saved_model.evaluate([X_test, L_test], y_test, verbose=1)
        print("Best model result: ", result)

    def __get_saved_model_name(self, ):
        '''
        {epoch:02d}-{val_lcm_precision_1k:.2f}
        '''
        if self.use_lcm and self.use_att:
            return "checkpoint_labs.h5"
        elif self.use_lcm:
            return "checkpoint_lbs.h5"
        elif self.use_att:
            return "checkpoint_lab.h5"
        else:
            return "checkpoint_b.h5"
