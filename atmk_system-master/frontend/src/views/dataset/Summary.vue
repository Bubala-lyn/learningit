<template>
  <div>
    <a-card title="数据分析">
      <a-button type="primary" style="width: 100px" @click="handleClick" :loading="loading">开始分析</a-button>
      <a-button @click="downloadQuestion('word')">下载题目长度（词）</a-button>
      <a-button @click="downloadQuestion('char')">下载题目长度（字）</a-button>
      <a-button @click="downloadKnowledge">下载知识点</a-button>
      <a-button @click="downloadWordCount">下载词频</a-button>
      <br />
      <br />
      <a-descriptions bordered layout="vertical" :column="4">
        <a-descriptions-item label="题目数量" :span="4">{{ question.count }}</a-descriptions-item>
        <a-descriptions-item label="题目平均字数"> {{ question.avg_char }} </a-descriptions-item>
        <a-descriptions-item label="题目平均词数"> {{ question.avg_word }} </a-descriptions-item>
        <a-descriptions-item label="题目平均公式数"> {{ question.avg_formula }} </a-descriptions-item>
        <a-descriptions-item label="题目平均知识点数"> {{ question.avg_label }} </a-descriptions-item>
        <a-descriptions-item label="标签数量" :span="2"> {{ label.count }} </a-descriptions-item>
        <a-descriptions-item label="标签平均标记数" :span="2"> {{ label.avg_tag }} </a-descriptions-item>
        <a-descriptions-item label="标签排序" :span="4">
          <a-table rowKey="id" bordered :dataSource="labelTags" :columns="labelColumns" />
          <!-- TODO 柱状图 -->
        </a-descriptions-item>
      </a-descriptions>
    </a-card>
    <br />
    <a-card title="试题ID查询">
      <p>输入切词后的文本：</p>
      <a-textarea v-model="content" :rows="8" />
      <br />
      <br />
      <a-button @click="onSearch">提交</a-button>
      <br />
      <br />
      <div>试题：{{ result }}</div>
    </a-card>
  </div>
</template>
<script>
  import { getDataSummary, postSearchQuestion, postWordCount } from '@/api/system'
  import { labelMixin } from '@/store/dataset-mixin'
  export default {
    name: 'Summary',
    data() {
      return {
        loading: false,
        question: {},
        label: {},
        labelTags: [],
        content: '',
        result: null
      }
    },
    computed: {
      labelColumns() {
        const columns = [
          {
            title: '序号',
            dataIndex: 'id'
          },
          {
            title: '知识点',
            key: 'name',
            customRender: (text, record) => this.getLabelName(record.id)
          },
          {
            title: '标记次数',
            dataIndex: 'num'
          }
        ]

        return columns
      }
    },
    mixins: [labelMixin],
    methods: {
      handleClick() {
        this.loading = true
        getDataSummary()
          .then((res) => {
            const data = res.data
            const { question, label } = data
            this.question = question
            this.label = label
            this.labels = data.label_tags
            this.wordLens = data.word_formula_dis
            this.charLens = data.char_formula_dis
            this.formatTags(data.label_tags)
          })
          .catch((error) => {
            console.log('getDataSummary', error)
          })
          .finally(() => {
            this.loading = false
          })
      },
      formatTags(tagRet) {
        const temp = Object.entries(tagRet)
          .map((item) => ({ id: Number(item[0]), num: item[1] }))
          .sort((a, b) => b.num - a.num)

        this.labelTags = temp
      },
      onSearch() {
        postSearchQuestion({ content: this.content }).then((res) => {
          console.log(res.data)
          this.result = res.data.id
        })
      },
      downloadQuestion(type) {
        const data = type === 'word' ? this.wordLens : this.charLens
        let str = 'len,num\r\n'
        for (const key in data) {
          str += `${key},${data[key]}\r\n`
        }
        str = 'data:application/csv,' + encodeURIComponent(str)
        const link = document.createElement('a')
        link.setAttribute('href', str)
        link.setAttribute('download', `${type}.csv`)
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      },
      downloadKnowledge() {
        let str = 'id,num\r\n'
        for (const key in this.labels) {
          str += `${key},${this.labels[key]}\r\n`
        }
        str = 'data:application/csv,' + encodeURIComponent(str)
        const link = document.createElement('a')
        link.setAttribute('href', str)
        link.setAttribute('download', 'labels.csv')
        document.body.appendChild(link)
        link.click()
        document.body.removeChild(link)
      },
      downloadWordCount() {
        postWordCount().then((res) => {
          const list = res.data.vocab_list
          let str = 'word count\r\n'
          let i = 0
          for (const key in list) {
            let k = key
            if (key.startsWith('[F]')) {
              k = `[F]${i++}`
            }
            str += `${k}  ${list[key]}\r\n`
          }
          str = 'data:application/csv,' + encodeURIComponent(str)
          const link = document.createElement('a')
          link.setAttribute('href', str)
          link.setAttribute('download', 'word_count.csv')
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
        })
      }
    }
  }
</script>
