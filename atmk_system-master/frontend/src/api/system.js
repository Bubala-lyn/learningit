import request from '@/utils/request'

const systemApi = {
  Label: '/math_questions/label_list',
  Question: '/math_questions/content_list',
  CleanResult: '/math_questions/cleaned_result',
  CleanQuestion: '/math_questions/clean_data',
  ReadVector: '/math_questions/read_vector',
  DataSummary: '/math_questions/data_summary',
  ManualTag: '/math_questions/manual_tag',
  ManualCheck: '/math_questions/manual_check',
  Preprocess: '/math_questions/preprocess',
  ParseFormula: '/math_questions/parse_formula',
  CleanItem: '/math_questions/clean_item',
  SearchQuestion: '/math_questions/search_question',
  WordCount: '/math_questions/word_count',
  MakeLabelEmb: '/math_questions/make_label_emb',
  SearchLabelEmb: '/math_questions/search_label_emb'
}

export function getMathKnowledge() {
  return request({
    url: systemApi.Label,
    method: 'get'
  })
}

export function getMathContent(params) {
  return request({
    url: systemApi.Question,
    method: 'post',
    data: params,
    timeout: 0
  })
}

export function getCleanResult(params) {
  return request({
    url: systemApi.CleanResult,
    method: 'get',
    params: params
  })
}

export function postCleanData(params) {
  return request({
    url: systemApi.CleanQuestion,
    method: 'post',
    data: params,
    timeout: 0
  })
}

export function getVectorByType(params) {
  return request({
    url: systemApi.ReadVector,
    method: 'post',
    data: params,
    timeout: 0
  })
}

export function getDataSummary(params) {
  return request({
    url: systemApi.DataSummary,
    method: 'get',
    params: params,
    timeout: 0
  })
}

// 人工打标签
export function tagQuestion(params) {
  return request({
    url: systemApi.ManualTag,
    method: 'post',
    data: params
  })
}

// 检查相同知识点的题目是否重复
export function checkContent(params) {
  return request({
    url: systemApi.ManualCheck,
    method: 'get',
    params: params,
    timeout: 0
  })
}

// 准备训练用训练集、验证集、测试集
export function postDataPrecess(params) {
  return request({
    url: systemApi.Preprocess,
    method: 'post',
    data: params,
    timeout: 0
  })
}

// 公式解析
export function parseFormula(params) {
  return request({
    url: systemApi.ParseFormula,
    method: 'post',
    data: params
  })
}

// 清洗用户输入的试题
export function cleanUserInput(params) {
  return request({
    url: systemApi.CleanItem,
    method: 'post',
    data: params
  })
}

// 查询试题
export function postSearchQuestion(params) {
  return request({
    url: systemApi.SearchQuestion,
    method: 'post',
    data: params
  })
}

// 词频统计
export function postWordCount() {
  return request({
    url: systemApi.WordCount,
    method: 'post',
    timeout: 0
  })
}

// 制作标签嵌入
export function postMakeLabels() {
  return request({
    url: systemApi.MakeLabelEmb,
    method: 'post',
    timeout: 0
  })
}

// 查询标签嵌入
export function postSearchLabelVector(params) {
  return request({
    url: systemApi.SearchLabelEmb,
    method: 'post',
    data: params,
    timeout: 0
  })
}
