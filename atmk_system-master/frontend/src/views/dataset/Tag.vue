<template>
  <div>
    <div v-for="item in list" :key="item.id">
      {{ item.id }}
      <img v-lazy="item.label_img" />
      <br />
      <span style="margin-right: 42px"></span>
      <span style="color:green" v-for="it in item.labels" :key="it">{{ getLabelName(it) }}；</span>
      <!-- <a-tag style="font-size: 14px" v-for="it in item.labels" :key="it">{{ getLabelName(it) }}</a-tag> -->
      <!-- <a-select
        size="small"
        mode="multiple"
        style="width: 80%"
        placeholder="请选择知识点"
        :filter-option="false"
        v-model="item.labels"
        @search="searchLabels"
      >
        <a-select-option v-for="it in filterLabels(item)" :key="it.uuid" :value="it.id">
          {{ it.name }}
        </a-select-option>
      </a-select>
      <a-button size="small" type="primary" @click="handleSubmit(item)">save</a-button> -->
      <a-divider />
    </div>
  </div>
</template>
<script>
  import { getMathContent, tagQuestion } from '@/api/system'
  import debounce from 'lodash.debounce'
  export default {
    name: 'Tag',
    data() {
      this.searchLabels = debounce(this.searchLabels, 800)
      return {
        searchKey: '',
        list: [],
        pagination: {
          pageSize: 2000,
          current: 13,
          total: 0
        }
      }
    },
    computed: {
      filterLabels() {
        return (question) => {
          const { labels } = this.$store.getters
          const { searchKey } = this
          let data = []
          if (searchKey === '') {
            data = labels.filter((item) => question.labels.includes(item.id))
          } else {
            data = labels.filter((item) => item.name.includes(this.searchKey))
          }
          return data
        }
      }
    },
    methods: {
      getData() {
        const { current, pageSize } = this.pagination
        getMathContent({
          page: current,
          size: pageSize,
          cond: []
        })
          .then((res) => {
            this.list = res.data.data
          })
          .catch((error) => {
            console.log('getMathContent error', error)
          })
      },
      searchLabels(value) {
        this.searchKey = value
      },
      getLabelName(id) {
        const { labels } = this.$store.getters
        const item = labels.find((item) => item.id === id)
        if (item) {
          return item.name
        } else {
          return id
        }
      },
      // 确认知识点标注
      handleSubmit(question) {
        const { id, labels } = question
        tagQuestion({ id, labels })
      }
    },
    created() {
      this.getData()
      this.$store.dispatch('getLabels')
    }
  }
</script>
