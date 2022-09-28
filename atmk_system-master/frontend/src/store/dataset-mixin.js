export const labelMixin = {
  methods: {
    getLabelName(id) {
      const { labels } = this.$store.getters
      const item = labels.find(item => item.id === id)
      if (item) {
        return item.name
      } else {
        return id
      }
    }
  },
  created() {
    this.$store.dispatch('getLabels')
  }
}
