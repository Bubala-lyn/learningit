import os
import logging

from Configuration.configuration import Configuration
from formula_parser import FormulaParser
from encoder_tuple_level import TupleEncoder, TupleTokenizationMode
from tangent_cft_module import TangentCFTModule


class TangentCFTBackEnd:
    def __init__(self, config_file, data_set, read_slt=True, query_formulas=None):
        self.data_reader = FormulaParser(
            data_set, read_slt, query_formulas)
        if config_file is not None:
            self.config = Configuration(config_file)
        self.encoder_map_node = {}
        self.encoder_map_edge = {}
        self.node_id = 60000
        self.edge_id = 500
        self.module = None

    def train_model(self, map_file_path, model_file_path=None,
                    embedding_type=TupleTokenizationMode.Both_Separated, ignore_full_relative_path=True,
                    tokenize_all=False, tokenize_number=True):
        """
        This method is for training the tangent-cft model and saves the encoder and model after training is done.
        """
        self.module = TangentCFTModule()
        if os.path.isfile(map_file_path):
            self.__load_encoder_map(map_file_path)
        dictionary_formula_tuples_collection = self.__encode_train_tuples(embedding_type, ignore_full_relative_path,
                                                                          tokenize_all, tokenize_number)
        self.__save_encoder_map(map_file_path)
        logging.info("training the fast text model...")
        self.module.train_model(self.config, list(
            dictionary_formula_tuples_collection.values()))

        if model_file_path is not None:
            logging.info("saving the fast text model...")
            self.module.save_model(model_file_path)
        return dictionary_formula_tuples_collection

    def load_model(self, map_file_path, model_file_path):
        """Loads tangent-cft models and encoder map. """
        self.module = TangentCFTModule(model_file_path)
        self.__load_encoder_map(map_file_path)

    def get_collection_query_vectors(self, embedding_type=TupleTokenizationMode.Both_Separated, ignore_full_relative_path=True,
                                     tokenize_all=False, tokenize_number=True):
        """
        This method returns vector representations for formula queries. The vectors are
        in numpy array and are returned in dictionary of formula id as key and vector as value.
        """
        dictionary_query_tuples = self.data_reader.get_query()
        query_vectors = {}
        for query in dictionary_query_tuples:
            encoded_tuple_query = self.__encode_lst_tuples(dictionary_query_tuples[query], embedding_type,
                                                           ignore_full_relative_path, tokenize_all, tokenize_number)
            query_vec = self.module.get_query_vector(encoded_tuple_query)
            query_vectors[query] = query_vec
        return query_vectors

    def get_formula_tuples(self, ):
        return self.data_reader.get_query()

    def get_formula_vectors(self, formula_tuples, embedding_type=TupleTokenizationMode.Both_Separated, ignore_full_relative_path=True,
                            tokenize_all=False, tokenize_number=True):
        query_vectors = {}
        for query in formula_tuples:
            encoded_tuple_query = self.__encode_lst_tuples(formula_tuples[query], embedding_type,
                                                           ignore_full_relative_path, tokenize_all, tokenize_number)
            try:
                query_vec = self.module.get_query_vector(encoded_tuple_query)
                query_vectors[query] = query_vec
            except Exception:
                query_vectors[query] = None
        return query_vectors

    def __encode_train_tuples(self, embedding_type, ignore_full_relative_path, tokenize_all, tokenize_number):
        """
        This methods read the collection queries in the dictionary of formula_id: tuple list and encodes the tuples according the criteria
        defined in the method inputs.
        The return value is dictionary of formula_id and list of encoded tuples
        """
        dictionary_lst_encoded_tuples = {}
        logging.info("reading train data...")
        dictionary_formula_slt_tuple = self.data_reader.get_collection()
        logging.info("dictionary_formula_slt_tuple length " +
                     str(len(dictionary_formula_slt_tuple.keys())))
        logging.info("encoding train data...")
        for formula in dictionary_formula_slt_tuple:
            dictionary_lst_encoded_tuples[formula] = self.__encode_lst_tuples(dictionary_formula_slt_tuple[formula],
                                                                              embedding_type, ignore_full_relative_path,
                                                                              tokenize_all,
                                                                              tokenize_number)
        return dictionary_lst_encoded_tuples

    def __encode_lst_tuples(self, list_of_tuples, embedding_type, ignore_full_relative_path, tokenize_all,
                            tokenize_number):
        """
        This methods takes list of tuples and encode them and return encoded tuples
        """
        encoded_tuples, update_map_node, update_map_edge, node_id, edge_id = \
            TupleEncoder.encode_tuples(self.encoder_map_node, self.encoder_map_edge, self.node_id, self.edge_id,
                                       list_of_tuples, embedding_type, ignore_full_relative_path, tokenize_all,
                                       tokenize_number)

        self.node_id = node_id
        self.edge_id = edge_id
        self.encoder_map_node.update(update_map_node)
        self.encoder_map_edge.update(update_map_edge)
        return encoded_tuples

    def __save_encoder_map(self, map_file_path):
        """
        This method saves the encoder used for tokenization of formula tuples.
        map_file_path: file path to save teh encoder map in form of TSV file with column E/N \t character \t encoded value
        where E/N shows if the character is edge or node value, the character is tuple character to be encoded and encoded
        value is the value the encoder gave to character.
        """
        file = open(map_file_path, "w", encoding="utf-8")
        for item in self.encoder_map_node:
            file.write("N" + "\t" + str(item) + "\t" +
                       str(self.encoder_map_node[item]) + "\n")
        for item in self.encoder_map_edge:
            file.write("E" + "\t" + str(item) + "\t" +
                       str(self.encoder_map_edge[item]) + "\n")
        file.close()

    def __load_encoder_map(self, map_file_path):
        """
        This method loads the saved encoder values into two dictionary used for edge and node values.
        """
        file = open(map_file_path, encoding="utf-8")
        line = file.readline().strip("\n")
        while line:
            parts = line.split("\t")
            encoder_type = parts[0]
            symbol = parts[1]
            value = int(parts[2])
            if encoder_type == "N":
                self.encoder_map_node[symbol] = value
            else:
                self.encoder_map_edge[symbol] = value
            line = file.readline().strip("\n")
        "The id shows the id that should be assigned to the next character to be encoded (a character that is not seen)" \
            "Therefore there is a plus one in the following lines"
        self.node_id = max(list(self.encoder_map_node.values())) + 1
        self.edge_id = max(list(self.encoder_map_edge.values())) + 1
        file.close()
