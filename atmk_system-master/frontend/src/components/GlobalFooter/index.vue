<template>
  <global-footer class="footer custom-render">
    <template v-slot:links>
      <a href="https://github.com/gaoshu883" target="_blank">Github</a>
    </template>
  </global-footer>
</template>

<script>
import { GlobalFooter } from '@ant-design-vue/pro-layout'

export default {
  name: 'ProGlobalFooter',
  components: {
    GlobalFooter
  }
}
</script>
