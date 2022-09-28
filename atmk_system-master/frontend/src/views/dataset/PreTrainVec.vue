<template>
  <a-card :loading="loading" title="向量查询">
    <p>题目：{{ demoData.text }}</p>
    <p>分字：{{ demoData.char_list }}</p>
    <p>分词：{{ demoData.word_list }}</p>
    <p>
      知识点：<a-tag v-for="item in demoData.label_list" :key="item">{{ getLabelName(item) }} </a-tag>
    </p>
    <a-input-group compact>
      <a-select v-model="charVersion" style="width: 100px">
        <a-select-option value="atmk"> atmk </a-select-option>
        <a-select-option value="baidu"> baidu </a-select-option>
      </a-select>
      <a-input-search
        style="width: 200px"
        placeholder="查询并下载字向量"
        @search="(value) => getVector(value, 'char')"
      />
    </a-input-group>
    <br />
    <a-input-group compact>
      <a-select v-model="wordVersion" style="width: 100px">
        <a-select-option value="atmk"> atmk </a-select-option>
        <a-select-option value="baidu"> baidu </a-select-option>
      </a-select>
      <a-input-search
        style="width: 200px"
        placeholder="查询并下载词向量"
        @search="(value) => getVector(value, 'word')"
      />
    </a-input-group>
    <div style="margin-top: 8px">(读取百度预训练词向量耗时长，每词或字符~2.5min)</div>
    <a-divider />
    <a-table :pagination="false" bordered :dataSource="demoData.formulas" :columns="columns">
      <template slot="action" slot-scope="name, record">
        <a href="javascript:;" style="margin-right: 8px" @click="getFormulaVector(record, 'atmk')">下载(atmk)</a>
        <a href="javascript:;" @click="getFormulaVector(record, 'wiki')">下载(wiki)</a>
      </template>
    </a-table>
  </a-card>
</template>
<script>
  import { getCleanResult, getVectorByType } from '@/api/system'
  import { downloadFile } from '@/utils/util'
  import { labelMixin } from '@/store/dataset-mixin'
  export default {
    name: 'PreTrainVec',
    data() {
      return {
        loading: false,
        charVersion: 'atmk',
        wordVersion: 'atmk',
        demoData: {
          text: '',
          formulas: [],
          label_list: [],
          char_list: [],
          word_list: []
        },
        columns: [
          {
            title: '模式',
            dataIndex: 'key',
            key: 'key',
            width: 200
          },
          {
            title: '公式',
            dataIndex: 'value',
            key: 'value'
          },
          {
            title: '解析',
            dataIndex: 'value',
            key: 'formula',
            customRender: (text) => (
              <div
                {...{
                  domProps: {
                    innerHTML: text
                  }
                }}
              ></div>
            )
          },
          {
            title: '向量',
            dataIndex: 'action',
            width: 200,
            scopedSlots: { customRender: 'action' }
          }
        ]
      }
    },
    mixins: [labelMixin],
    methods: {
      getData() {
        this.loading = true
        getCleanResult()
          .then((res) => {
            const data = res.data.demo_data
            const formulas = Object.entries(data.formulas).map(([key, value]) => ({ key, value }))
            Object.assign(this.demoData, data, { formulas })
          })
          .catch((error) => {
            console.log(error, 'getCleanResult...')
          })
          .finally(() => {
            this.loading = false
          })
      },
      getFormulaVector(record, version) {
        getVectorByType({
          ...record,
          type: 'formula',
          version
        })
          .then((res) => {
            downloadFile(JSON.stringify(res.data), `${record.key}.json`)
          })
          .catch((error) => {
            console.log('getFormulaVector ...', error)
          })
      },
      getVector(value, type) {
        getVectorByType({
          type: type,
          value: value,
          version: type === 'char' ? this.charVersion : this.wordVersion
        })
          .then((res) => {
            downloadFile(JSON.stringify(res.data), `${value}.json`)
          })
          .catch((error) => {
            console.log('getVector ...', error)
          })
      }
    },
    created() {
      this.getData()
    }
  }
</script>
