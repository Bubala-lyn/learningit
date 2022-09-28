<template>
  <a-card>
    <a-tree show-line :tree-data="treeData" />
    <br />
    <a-input-search
      style="width: 300px"
      v-model="searchKey"
      placeholder="请输入知识点id"
      enter-button="查询"
      @search="onSearch"
    />
    <br />
    <p>{{ labelName }}</p>
  </a-card>
</template>

<script>
  export default {
    name: 'Knowledge',
    data() {
      return {
        list: [],
        treeData: [],
        searchKey: '',
        labelName: ''
      }
    },
    methods: {
      mapTreeData(list, pid) {
        const children = []
        list
          .filter((item) => item.parent_uuid === pid)
          .forEach((item) => {
            const ret = {
              title: item.name,
              key: item.uuid
            }
            const subChild = this.mapTreeData(list, item.uuid)
            if (subChild.length > 0) ret.children = subChild
            children.push(ret)
          })
        return children
      },
      onSearch() {
        const target = this.list.find((item) => item.id + '' === this.searchKey)
        if (target) {
          this.labelName = target.name
        } else {
          this.labelName = ''
        }
      }
    },
    created() {
      this.$store.dispatch('getLabels')
    },
    watch: {
      '$store.getters.labels': {
        immediate: true,
        handler(list) {
          this.list = list
          const temp = this.mapTreeData(list, '')
          this.treeData = temp
        }
      }
    }
  }
</script>

<style scoped></style>
