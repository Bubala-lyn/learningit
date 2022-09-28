import logging

from TangentS.math_tan.math_extractor import MathExtractor


class FormulaParser():
    def __init__(self, formula_collection, read_slt=True, query_formulas=None):
        '''
        数据集的结构
        [
            {
                "content": "<math></math>", # 单个数学公式
                "key": "HOLEL_20000_WLDOR_1"
            },
        ]
        '''
        self.read_slt = read_slt
        self.formula_collection = formula_collection  # 数据集
        self.query_formulas = query_formulas  # 待查询公式

    def get_collection(self, ):
        """
        该方法从数据集中解析数学公式
        The return value is a dictionary of formula id (as key) and list of tuples (as value)
        """
        dictionary_formula_tuples = {}
        formula_collection = self.formula_collection
        for f in formula_collection:
            try:
                formulas = MathExtractor.parse_from_xml(f['content'], 1, operator=(not self.read_slt), missing_tags=None,
                                                        problem_files=None)
                for key in formulas:
                    tuples = formulas[key].get_pairs(
                        window=2, eob=True)
                    dictionary_formula_tuples[f['key']] = tuples
            except Exception as err:
                logging.info('math parsed error ' + f['key'])
                print(f['key'], err)
        return dictionary_formula_tuples

    def get_query(self, ):
        """
        获取待查询数据集中的数学公式
        待查询数据集结构
        [
            {
                "content": "<math></math>",
                "key": "HOLEL_20000_WLDOR_1"
            },
        ]
        """
        dictionary_query_tuples = {}
        query_formulas = self.query_formulas
        for f in query_formulas:
            try:
                formulas = MathExtractor.parse_from_xml(f['content'], 1, operator=(not self.read_slt), missing_tags=None,
                                                        problem_files=None)
                for key in formulas:
                    tuples = formulas[key].get_pairs(window=2, eob=True)
                    dictionary_query_tuples[f['key']] = tuples
            except Exception as err:
                logging.info('math parsed error ' + f['key'])
                print(f['key'], err)
        return dictionary_query_tuples
