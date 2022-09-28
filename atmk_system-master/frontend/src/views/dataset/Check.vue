<template>
  <div>
    <p v-for="(item, index) in contents" :key="index">
      <a-card :title="item.label">
        <p v-for="(it, idx) in item.content" :key="idx">
          <span>{{ it.id }}</span> <span>{{ it.text }}</span>
        </p>
      </a-card>
    </p>
  </div>
</template>
<script>
  import { checkContent } from '@/api/system'
  export default {
    name: 'ManualCheck',
    data() {
      return {
        contents: []
      }
    },
    methods: {
      getData() {
        checkContent()
          .then((res) => {
            this.contents = res.data
          })
          .catch((error) => {
            console.log(error, 'checkContent')
          })
      }
    },
    created() {
      // this.getData()
    }
  }
</script>
