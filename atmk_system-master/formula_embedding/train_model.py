from tangent_cft_back_end import TangentCFTBackEnd

import logging
import pickle

logging.basicConfig(
    level=logging.INFO,
    filename='train.log',
    filemode='a',
    format='%(asctime)s : %(levelname)s : %(message)s',
)


def main():
    read_slt = True  # 采集到的数学公式是 Presentation MathML
    config_file = 'Configuration/config/config_1'  # fasttext配置文件
    dataset_file_path = '../file_data/math_questions_content.pkl'
    model_file_path = '../file_data/da-20k/slt_model'  # Model file path
    map_file_path = '../file_data/da-20k/slt_encoder.tsv'

    '''
    获取公式collection
    [
        {
            "content": "<math></math>",
            "key": "HOLEL_20000_WLDOR_1"
        },
    ]
    公式训练
    '''
    data_set = []
    with open(dataset_file_path, 'rb') as data_f_pickle:
        data = pickle.load(data_f_pickle)
        for u in data:
            formulas = u['formulas']
            for key in formulas:
                data_set.append({
                    'key': key,
                    'content': formulas[key]
                })
    system = TangentCFTBackEnd(
        config_file=config_file, data_set=data_set, read_slt=read_slt)
    system.train_model(
        map_file_path=map_file_path,
        model_file_path=model_file_path,
        tokenize_all=False,
        tokenize_number=True
    )


if __name__ == "__main__":
    main()
