<template>
  <div class="page-transition-container">
    <Transition name="fade" mode="out-in">
      <slot />
    </Transition>
  </div>
</template>

<script setup>
// 页面过渡组件，用于实现页面切换时的过渡效果
</script>

<style scoped>
.page-transition-container {
  width: 100%;
  height: 100%;
}

/* 淡入淡出过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 滑动过渡效果 */
.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.slide-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

/* 缩放过渡效果 */
.scale-enter-active,
.scale-leave-active {
  transition: all 0.3s ease;
}

.scale-enter-from,
.scale-leave-to {
  transform: scale(0.9);
  opacity: 0;
}
</style>