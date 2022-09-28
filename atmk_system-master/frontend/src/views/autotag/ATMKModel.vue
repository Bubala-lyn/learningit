<template>
  <div>
    <a-card title="数据预处理">
      <a-form layout="inline">
        <a-form-item label="文本切分类型">
          <a-select v-model="textType" style="width: 100px">
            <a-select-option value="word"> word </a-select-option>
            <a-select-option value="char"> char </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="文本预训练向量版本">
          <a-select v-model="textVersion" style="width: 100px">
            <a-select-option value="atmk"> atmk </a-select-option>
            <a-select-option value="baidu"> baidu </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="公式预训练向量版本">
          <a-select v-model="formulaVersion" style="width: 100px">
            <a-select-option value="atmk"> atmk </a-select-option>
            <a-select-option value="wiki"> wiki </a-select-option>
          </a-select>
        </a-form-item>
        <br />
        <br />
        <p>
          <a-button type="primary" @click="preprocess" :loading="loading">准备数据集</a-button>
          <a-input-search
            v-model="vocab"
            style="margin-left: 32px; width: 300px"
            placeholder="请输入词汇"
            enter-button="查询向量"
            @search="onSearchVector"
          />
        </p>
        <p>
          <a-button type="primary" @click="makeLabel">准备标签嵌入</a-button>
          <a-input-search
            v-model="labelId"
            style="margin-left: 32px; width: 300px"
            placeholder="请输入标签ID"
            enter-button="查询向量"
            @search="onSearchLabelVector"
          />
        </p>
        <p>
          数据集：<a-tag>{{ filenames.math_data }}</a-tag> 词表：<a-tag>{{ filenames.vocab_label }}</a-tag>
          词向量：<a-tag>{{ filenames.embeddings }}</a-tag>
        </p>
      </a-form>
    </a-card>
    <br />
    <a-card title="模型训练">
      <a-button type="primary" style="width: 100px">操作</a-button>
    </a-card>

    <br />
    <a-card title="读取单条数据">
      <p>
        如何切公式
        <a-select v-model="formulaType">
          <a-select-option :value="1"> 整体处置 </a-select-option>
          <a-select-option :value="2">按纯文本处置</a-select-option>
          <a-select-option :value="3">过滤公式</a-select-option>
        </a-select>
      </p>
      <a-textarea v-model="htmlContent" :rows="8" />
      <br />
      <br />
      <a-input v-model="labels" placeholder="请输入本题知识点IDs，以,分隔" />
      <br />
      <br />
      <a-button @click="getInputVector">下载测试数据</a-button>
      <br />
      <br />
      <div>{{ dataResult }}</div>
    </a-card>
  </div>
</template>
<script>
  import { postDataPrecess, getVectorByType, cleanUserInput, postMakeLabels, postSearchLabelVector } from '@/api/system'
  import { downloadFile } from '@/utils/util'

  export default {
    name: 'ATMKModel',
    data() {
      return {
        loading: false,
        vocab: '',
        textType: 'word',
        textVersion: 'atmk',
        formulaVersion: 'atmk',
        filenames: {},
        formulaType: 1,
        htmlContent: '',
        labels: '',
        dataResult: null,
        labelId: ''
      }
    },
    methods: {
      preprocess() {
        this.loading = true
        postDataPrecess({
          text_type: this.textType,
          text_version: this.textVersion,
          formula_version: this.formulaVersion
        })
          .then((res) => {
            console.log(res)
            Object.assign(this.filenames, res.data)
            this.$message.success('操作成功！')
          })
          .catch((error) => {
            console.log('postDataPrecess ...', error)
          })
          .finally(() => {
            this.loading = false
          })
      },
      onSearchVector() {
        getVectorByType({
          type: 'vocab',
          value: this.vocab
        })
          .then((res) => {
            downloadFile(JSON.stringify(res.data), `${this.vocab}.json`)
          })
          .catch((error) => {
            console.log('getFormulaVector ...', error)
          })
      },
      getInputVector() {
        const labels = this.labels
          .split(',')
          .map((item) => Number(item))
          .filter(Boolean)
        cleanUserInput({
          content: this.htmlContent,
          formula_cut_type: this.formulaType,
          label_list: labels
        })
          .then((res) => {
            downloadFile(JSON.stringify(res.data), 'inputVector.json')
          })
          .catch((error) => {
            console.log('cleanUserInput', error)
          })
      },
      makeLabel() {
        postMakeLabels()
      },
      onSearchLabelVector() {
        postSearchLabelVector({ label_id: this.labelId })
      }
    }
  }
</script>
