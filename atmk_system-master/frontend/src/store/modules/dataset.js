import { SET_QUESTION_LABELS, SET_QUESTION_LABELS_LOADING, RESET_QUESTION_LABELS } from '@/store/mutation-types'
import { getMathKnowledge } from '@/api/system'

const dataset = {
  state: {
    knowledge: {
      loading: false,
      loaded: false,
      list: []
    }
  },
  getters: {
    labels(state) {
      return state.knowledge.list
    }
  },
  mutations: {
    [SET_QUESTION_LABELS]: (state, data) => {
      Object.assign(state.knowledge, {
        loading: false,
        loaded: true,
        list: data
      })
    },
    [SET_QUESTION_LABELS_LOADING]: (state, loading) => {
      state.knowledge.loading = loading
    },
    [RESET_QUESTION_LABELS]: state => {
      Object.assign(state.knowledge, {
        loading: false,
        loaded: false,
        list: []
      })
    }
  },
  actions: {
    getLabels({ state, commit }) {
      const { loading, loaded } = state.knowledge
      if (loading || loaded) return
      commit(SET_QUESTION_LABELS_LOADING, true)
      getMathKnowledge()
        .then(res => {
          commit(SET_QUESTION_LABELS, res.data)
        })
        .catch(error => {
          commit(RESET_QUESTION_LABELS)
          console.log('getMathKnowledge error', error)
        })
    }
  }
}

export default dataset
