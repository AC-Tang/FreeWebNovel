import { defineStore } from 'pinia'

export const useHeaderStore = defineStore('header', {
  state: () => ({
    needsWhiteText: false // 是否需要白色文字
  }),

  actions: {
    // 更新导航栏文字颜色
    updateTextColor(needsWhite) {
      this.needsWhiteText = needsWhite
    }
  }
})