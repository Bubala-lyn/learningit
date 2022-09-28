import keras
from keras.models import Model
from keras.layers import Input, Dense, LSTM, Embedding, Bidirectional
from keras.layers import Flatten, Concatenate, Permute, Lambda, Dot
import keras.backend as K


class Classifier(object):
    """
    分类器
    """
    @classmethod
    def build(self, config, embedding_matrix=None, use_att=False, label_emb_matrix=None, metrics=None):

        maxlen = config.maxlen
        vocab_size = config.vocab_size
        wvdim = config.emb_size
        hidden_size = config.hidden_size
        num_classes = config.num_classes

        text_input = Input(shape=(maxlen,), name='text_input')
        if embedding_matrix is None:
            input_emb = Embedding(
                vocab_size, wvdim, input_length=maxlen, name='text_emb')(text_input)  # (V,wvdim)
        else:
            input_emb = Embedding(vocab_size, wvdim, input_length=maxlen, weights=[
                                  embedding_matrix], trainable=False, name='text_emb')(text_input)  # (V,wvdim)
        # NOTE 使用注意力则返回全部step，否则返回最后step
        # shape=(None, maxlen, hidden_size * 2) or shape=(None, hidden_size * 2)
        lstm_output = Bidirectional(LSTM(hidden_size, return_sequences=use_att))(
            input_emb)
        # 标签
        label_input = Input(shape=(num_classes,), name='label_input')

        # 标签预训练
        if label_emb_matrix is None:
            # shape=(None, num_classes, wvdim)
            label_emb = Embedding(
                num_classes, wvdim, input_length=num_classes, name='label_emb')(label_input)
        else:
            label_emb = Embedding(num_classes, wvdim, input_length=num_classes, weights=[
                                  label_emb_matrix], trainable=False, name='label_emb')(label_input)
        if use_att:  # 标签注意力
            # shape=(None, hidden_size)
            label_att_emb = Dense(hidden_size, activation='tanh',
                                  name='label_att_emb')(label_emb)  # shape=(None, num_classes, hidden_size*2)
            time_steps = K.int_shape(lstm_output)[1]
            # keras 的Permute与tensorflow 的tf.transpose相同作用
            # K.batch_dot不是对层进行的操作，需要用Lambda进行封装
            # NOTE 总之数学运算都需要用Lambda进行封装
            h1 = Lambda(lambda x: x[:, :, :hidden_size],
                        name="f_h")(lstm_output)
            h2 = Lambda(lambda x: x[:, :, hidden_size:],
                        name="b_h")(lstm_output)
            m1 = Lambda(lambda x: K.batch_dot(
                *x))([label_att_emb, Permute((2, 1))(h1)])
            m2 = Lambda(lambda x: K.batch_dot(
                *x))([label_att_emb, Permute((2, 1))(h2)])
            m1_probs = Dense(time_steps, activation='sigmoid')(m1)
            m2_probs = Dense(time_steps, activation='sigmoid')(m2)
            # # shape=(None, 427, 1024)
            label_att_mul = Concatenate(axis=2)(
                [Lambda(lambda x: K.batch_dot(*x))([m1_probs, h1]),
                 Lambda(lambda x: K.batch_dot(*x))([m2_probs, h2])])
            # shape=(None, hidden_size * 2)
            lstm_output = Lambda(lambda x: K.sum(
                x, 1)/num_classes, name="att_context")(label_att_mul)

        pred_probs = Dense(num_classes, activation='sigmoid',
                           name='pred_probs')(lstm_output)

        model = Model(inputs=[text_input, label_input], outputs=pred_probs)
        # 每一批次评估一次
        model.compile(loss='binary_crossentropy',
                      optimizer='Adam', metrics=metrics)  # 自定义评价函数
        model._get_distribution_strategy = lambda: None  # fix bug for 2.1 tensorboard
        print(model.summary())
        return model, lstm_output, label_emb
