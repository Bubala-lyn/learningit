from math import ceil
from django.http import HttpResponse
from django.contrib.auth.decorators import login_required
from django.views.decorators.http import require_POST
from math_questions.models import Knowledge, Content, KnowledgeTag
from atmk_system.utils import response_success, response_error, collect
from django.forms.models import model_to_dict
from .const import CACHE_FILE_PICKLE, CACHE_MATH_DATA, CACHE_VOCAB_LABEL, CACHE_EMNEDDINGS, CACHE_LABEL_EMNEDDINGS, MAX_LENGTH

from .utils import clean_html, remove_same, cut_word, cut_char

from MathByte.embeddings import Embeddings
from MathByte.preprocess import DataPreprocess
from formula_embedding.tangent_cft_back_end import TangentCFTBackEnd

import json
import time
import pickle
import os
import random
import numpy as np


@login_required
@require_POST
def questions(request):
    data = json.loads(request.body)
    page = data.get('page')
    size = data.get('size')
    cond = data.get('cond')
    data, count = collect(Content, page=page, size=size, conditions=cond)
    for u in data:
        query_set = KnowledgeTag.objects.filter(qid=u['id'])
        ret = []
        for query in query_set:
            temp = model_to_dict(query)
            ret.append(temp['label_id'])
        u['labels'] = ret
        u['clean_text'], a, b, c, d, e, f, u['formula_tuples'] = clean_html(
            u['text'], u['id'])
    return response_success(data={
        'data': data,
        'count': count
    })


@login_required
def labels(request):
    data, count = collect(Knowledge)
    return response_success(data=data)


@login_required
@require_POST
def tag(request):
    data = json.loads(request.body)
    qid = data.get('id')
    labels = data.get('labels')
    query_set = KnowledgeTag.objects.filter(qid=qid).values('label_id')
    label_ids = []
    for query in list(query_set):
        id = query['label_id']
        label_ids.append(id)
        if id not in labels:
            label = KnowledgeTag.objects.get(qid=qid, label_id=id)
            label.delete()
    for id in labels:
        if id not in label_ids:
            KnowledgeTag.objects.create(qid=qid, label_id=id)
        else:
            label = KnowledgeTag.objects.get(qid=qid, label_id=id)
            label.updated_at = int(time.time())
            label.save()
    return response_success(data={})


@login_required
@require_POST
def clean(request):
    '''
    从数据库读取题目，清洗、去重、提取公式、分析后保存到文件
    清洗掉小于n标记数的知识点
    清洗掉小于m字符数的题目
    [
        {
            "id": 1,
            "text":"题目文本",
            "math_text": "题目文本 with formulas",
            "char_list": [],
            "char_formula_list": [],
            "word_list": [],
            "word_formula_list": [],
            "label_list": [],
            "formulas": {
                "HEL_45293_WLDOR_1_OL": "mathML"
            },
            "formula_tuples": {
                "HEL_45293_WLDOR_1_OL": ["N!1\t0!\tn\twe", "M!()1x2\t0!\tn\t-"]
            }
        }
    ]
    '''
    data = json.loads(request.body)
    tag_min = data.get('tag_min')
    char_min = data.get('char_min')
    formula_cut_type = data.get('formula_cut_type')

    # 先拿出来有效的标签数据（标记数>=tag_min）
    effect_label_list = []
    for query in list(Knowledge.objects.all().values('id')):
        label_id = query['id']
        tags = KnowledgeTag.objects.filter(
            label_id=label_id).values('qid').distinct()
        if len(tags) >= tag_min:
            effect_label_list.append(label_id)

    temp = []
    for u in list(Content.objects.all().values('text', 'id')):
        ret = {}
        qid = u['id']
        # 读取题目的知识点列表
        label_list = []
        for query in list(KnowledgeTag.objects.filter(
                qid=qid).values('label_id').distinct()):
            label_id = query['label_id']
            if label_id in effect_label_list:
                label_list.append(label_id)

        # 排除无标签数据
        if label_list:
            ret['id'] = qid
            ret['label_list'] = label_list
            ret['text'], ret['formulas'], ret['math_text'], \
                ret['char_list'], ret['word_list'], \
                ret['char_formula_list'], ret['word_formula_list'], \
                ret['formula_tuples'] = clean_html(
                u['text'], qid, formula_cut_type)
            # 确保字符数>=char_min
            if len(ret['char_list']) >= char_min:
                temp.append(ret)

    clean_list = remove_same(temp)

    try:
        with open(CACHE_FILE_PICKLE, 'wb') as target_file:
            pickle.dump(clean_list, target_file)
    except:
        return response_error('clean error')
    return response_success(data={
        'file_name': CACHE_FILE_PICKLE,
        'updated_at': int(time.time()),
        'demo_data': clean_list[0],
    })


@login_required
def cleaned_data(request):
    try:
        updated_at = os.path.getmtime(CACHE_FILE_PICKLE)
        with open(CACHE_FILE_PICKLE, 'rb') as data_f_pickle:
            temp = pickle.load(data_f_pickle)
            count = len(temp)
            idx = random.randint(0, count - 1)
        return response_success(data={
            'file_name': CACHE_FILE_PICKLE,
            'updated_at': int(updated_at),
            'demo_data': temp[idx],
        })
    except:
        return response_success(data={})


@login_required
def data_summary(request):
    # 分析平衡后的数据
    try:
        with open(CACHE_FILE_PICKLE, 'rb') as data_f_pickle:
            questions = pickle.load(data_f_pickle)
            MAX_INT = 100000

            total_char = 0
            min_char = MAX_INT
            max_char = 0
            avg_char = 0
            total_word = 0
            min_word = MAX_INT
            max_word = 0
            avg_word = 0
            total_formula = 0
            min_formula = MAX_INT
            max_formula = 0
            avg_formula = 0
            total_label = 0
            min_label = MAX_INT
            max_label = 0
            avg_label = 0
            label_set = set()
            w_f_ret = {}
            c_f_ret = {}
            for u in questions:
                char_list_len = len(u['char_list'])
                total_char += char_list_len
                min_char = min(char_list_len, min_char)
                max_char = max(char_list_len, max_char)
                word_list_len = len(u['word_list'])
                total_word += word_list_len
                min_word = min(word_list_len, min_word)
                max_word = max(word_list_len, max_word)
                formula_len = len(u['formulas'])
                total_formula += formula_len
                min_formula = min(formula_len, min_formula)
                max_formula = max(formula_len, max_formula)
                label_len = len(u['label_list'])
                total_label += label_len
                min_label = min(label_len, min_label)
                max_label = max(label_len, max_label)
                # 用于统计不重复label数
                label_set.update(u['label_list'])
                # 统计试题长度
                # 词+公式
                w_f_len = str(ceil(word_list_len+formula_len))
                if w_f_len in w_f_ret:
                    w_f_ret[w_f_len] += 1
                else:
                    w_f_ret[w_f_len] = 1
                # 字+公式
                c_f_len = str(ceil(char_list_len+formula_len))
                if c_f_len in c_f_ret:
                    c_f_ret[c_f_len] += 1
                else:
                    c_f_ret[c_f_len] = 1
            l_count = len(label_set)
            q_count = len(questions)
            avg_char = round(total_char / q_count, 2)
            avg_word = round(total_word / q_count, 2)
            avg_formula = round(total_formula / q_count, 2)
            avg_label = round(total_label / q_count, 2)

            ret = {}
            total_tag = 0
            min_tag = MAX_INT
            max_tag = 0
            avg_tag = 0
            for label_id in label_set:
                query_set = KnowledgeTag.objects.filter(
                    label_id=label_id).values('qid').distinct()
                tag_len = len(list(query_set))
                ret[str(label_id)] = tag_len
                total_tag += tag_len
                min_tag = min(tag_len, min_tag)
                max_tag = max(tag_len, max_tag)
            avg_tag = round(total_tag / l_count, 2)

        return response_success(data={
            'question': {
                'count': q_count,  # 题目数量
                'min_char': min_char,  # 最小字符数
                'max_char': max_char,  # 最大字符数
                'avg_char': avg_char,  # 平均字符数
                'min_word': min_word,  # 最小词数
                'max_word': max_word,  # 最大词数
                'avg_word': avg_word,  # 平均词数
                'min_formula': min_formula,  # 最小公式数
                'max_formula': max_formula,  # 最大公式数
                'avg_formula': avg_formula,  # 平均公式数
                'min_label': min_label,  # 最小标签数
                'max_label': max_label,  # 最大标签数
                'avg_label': avg_label,  # 平均标签数
            },
            'label': {
                'count': l_count,  # 标签数
                'min_tag': min_tag,  # 标记最小数
                'max_tag': max_tag,  # 标记最大数
                'avg_tag': avg_tag,  # 平均标记数
            },
            'label_tags': ret,  # 每个标签对应的标记数
            'word_formula_dis': w_f_ret,  # 词公式长度分布
            'char_formula_dis': c_f_ret  # 字公式长度分布
        })
    except Exception as e:
        print(e)
        return response_success(data={})


@login_required
@require_POST
def preprocess(request):
    '''
    准备训练集、验证集、测试集
    准备词表（字+公式 or 词+公式）、预训练向量、标签表

    '''
    data = json.loads(request.body)
    text_type = data.get('text_type')
    text_version = data.get('text_version')
    formula_version = data.get('formula_version')

    if not os.path.exists(CACHE_MATH_DATA) \
        or not os.path.exists(CACHE_VOCAB_LABEL) \
            or not os.path.exists(CACHE_EMNEDDINGS):
        p = DataPreprocess(text_type, text_version, formula_version, CACHE_FILE_PICKLE,
                           CACHE_MATH_DATA, CACHE_VOCAB_LABEL, CACHE_EMNEDDINGS)
        p.run_task(MAX_LENGTH)
    return response_success(data={
        'math_data': CACHE_MATH_DATA,
        'vocab_label': CACHE_VOCAB_LABEL,
        'embeddings': CACHE_EMNEDDINGS,
    })


@login_required
@require_POST
def make_label_emb(request):
    '''准备标签嵌入'''
    label_set = set()
    with open(CACHE_FILE_PICKLE, 'rb') as data_f_pickle:
        questions = pickle.load(data_f_pickle)
        for u in questions:
            label_set.update(u['label_list'])
    data, count = collect(Knowledge)
    temp = []
    for u in data:
        if u['id'] in label_set:
            label_word_list = cut_word(u['name'])
            temp.append((u['id'], label_word_list))
    p = DataPreprocess(dataset_path=CACHE_FILE_PICKLE)
    p.create_label_emb(labels=temp, cache_label_file=CACHE_LABEL_EMNEDDINGS)

    return response_success(data={})


@login_required
@require_POST
def search_label_emb(request):
    '''查询标签嵌入'''
    data = json.loads(request.body)
    label_id = int(data.get('label_id'))
    query_set = Knowledge.objects.filter(id=label_id).values('name')
    name = list(query_set)[0]['name']
    label_word_list = cut_word(name)
    system = Embeddings(CACHE_VOCAB_LABEL,)
    emb = system.get_vector_of_label(label_id, CACHE_LABEL_EMNEDDINGS)
    result = {}
    result["emb"] = emb.tolist()
    result["words"] = label_word_list
    return response_success(data=result)


@login_required
@require_POST
def read_vector(request):
    '''
    读取字、数学公式向量
    '''
    data = json.loads(request.body)
    type_id = data.get('type')
    value = data.get('value')
    version = data.get('version')
    result = {}
    system = Embeddings(CACHE_VOCAB_LABEL, CACHE_EMNEDDINGS)
    if type_id == 'char':
        char_list = cut_char(value)  # 按字切分
        for char in char_list:
            result[char] = system.read_text_vec(type_id=type_id,
                                                query=char, version=version).tolist()
    elif type_id == 'word':
        word_list = cut_word(value)
        for word in word_list:
            result[word] = system.read_text_vec(type_id=type_id,
                                                query=word, version=version).tolist()
    elif type_id == 'formula':
        result[data.get('key')] = system.read_formula_vec(
            query_formula=value, version=version).tolist()
    else:
        result[value] = system.get_vector_of_vocab(value).tolist()

    return response_success(data=result)


@login_required
def check_same_label(request):
    '''拉取相同知识点的题目'''
    try:
        with open(CACHE_FILE_PICKLE, 'rb') as data_f_pickle:
            questions = pickle.load(data_f_pickle)
            ret = {}
            for u in questions:
                labels = u['label_list']
                labels.sort()  # 排序
                label_str = ','.join('%s' % id for id in labels)
                if label_str in ret:
                    ret[label_str].append(u)
                else:
                    ret[label_str] = []
            # 默认返回长度大于1的
            temp = []
            for label in ret:
                content_list = ret[label]
                if len(content_list) > 1:
                    temp.append({
                        'label': label,
                        'content': content_list
                    })
        return response_success(data=temp)
    except Exception as e:
        print(e)
        return response_success(data={})


@login_required
def read_encoded_tuple(request):
    '''
    读取数学公式解析编码后的元组
    '''
    data = json.loads(request.body)
    cond = data.get('cond')
    system = TangentCFTBackEnd(
        config_file=None, data_set=None, query_formulas=cond)
    formula_tuples = system.get_formula_tuples()
    return response_success(data=formula_tuples)


@login_required
@require_POST
def clean_item(request):
    '''
    清洗用户输入的试题文本
    '''
    data = json.loads(request.body)
    html = data.get('content')
    label_list = data.get('label_list')
    formula_cut_type = data.get('formula_cut_type')
    ret = {}
    ret['text'], ret['formulas'], ret['math_text'], \
        ret['char_list'], ret['word_list'], \
        ret['char_formula_list'], ret['word_formula_list'], \
        ret['formula_tuples'] = clean_html(html, 1, formula_cut_type)

    # 读取词表
    vocab_pickle = open(CACHE_VOCAB_LABEL, 'rb')
    word2index, label2index = pickle.load(vocab_pickle)
    vocab_pickle.close()
    PAD_ID = word2index.get('<pad>')
    UNK_ID = word2index.get('<unk>')
    word_index_list = []
    word_formula_list = ret['word_formula_list']
    formula_tuples = ret['formula_tuples']
    for word in word_formula_list:
        if word in formula_tuples:
            f = '[F]' + '⌘'.join(formula_tuples[word])
            word_index_list.append(word2index.get(
                f, UNK_ID))
        else:
            word_index_list.append(word2index.get(
                word, UNK_ID))
    pad_size = MAX_LENGTH
    if len(word_index_list) < pad_size:
        word_index_list.extend(
            [PAD_ID] * (pad_size - len(word_index_list)))
    else:
        word_index_list = word_index_list[:pad_size]
    ret['text_x'] = word_index_list
    if label_list:
        label_list_dense = [label2index[l]
                            for l in label_list]
        result = np.zeros(len(label2index))
        result[label_list_dense] = 1
        ret['text_y'] = result.tolist()
    return response_success(data=ret)


@login_required
@require_POST
def search_question(request):
    '''
    查询试题id
    '''
    data = json.loads(request.body)
    content = data.get('content')
    question = None
    try:
        with open(CACHE_FILE_PICKLE, 'rb') as data_f_pickle:
            questions = pickle.load(data_f_pickle)
            for u in questions:
                ret = []
                word_formula_list = u['word_formula_list']
                formula_tuples = u['formula_tuples']
                for word in word_formula_list:
                    if word in formula_tuples:
                        ret.append('[F]' + '⌘'.join(formula_tuples[word]))
                    else:
                        ret.append(word)
                text = ','.join(ret)
                if text in content:
                    question = u
        return response_success(data=question)
    except Exception as e:
        print(e)
        return response_success(data={})


@login_required
@require_POST
def word_count(request):
    text_type = 'word'
    text_version = 'atmk'
    formula_version = 'atmk'

    p = DataPreprocess(text_type, text_version,
                       formula_version, CACHE_FILE_PICKLE)
    x, y, vocab_list = p.create_vocab_label2index()
    '''统计词频'''
    ret = {}
    for v in vocab_list:
        if v not in ret:
            ret[v] = 1
        else:
            ret[v] += 1
    print(len(x), len(vocab_list))
    return response_success(data={
        'word2index': x,
        'vocab_list': vocab_list
    })
