<template>
  <div class="skeleton-container">
    <div class="skeleton-item" v-for="i in count" :key="i">
      <div class="skeleton-avatar" v-if="showAvatar"></div>
      <div class="skeleton-content">
        <div class="skeleton-title" v-if="showTitle"></div>
        <div class="skeleton-text" v-for="j in textLines" :key="j"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  count: {
    type: Number,
    default: 3
  },
  showAvatar: {
    type: Boolean,
    default: true
  },
  showTitle: {
    type: Boolean,
    default: true
  },
  textLines: {
    type: Number,
    default: 2
  }
})
</script>

<style scoped>
.skeleton-container {
  width: 100%;
}

.skeleton-item {
  display: flex;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.skeleton-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  margin-right: 15px;
  flex-shrink: 0;
}

.skeleton-content {
  flex: 1;
}

.skeleton-title {
  height: 20px;
  width: 60%;
  margin-bottom: 10px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-text {
  height: 16px;
  width: 100%;
  margin-bottom: 8px;
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: loading 1.5s infinite;
  border-radius: 4px;
}

.skeleton-text:last-child {
  width: 80%;
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@media (max-width: 768px) {
  .skeleton-item {
    padding: 10px;
  }
  
  .skeleton-avatar {
    width: 40px;
    height: 40px;
  }
  
  .skeleton-title {
    height: 18px;
  }
  
  .skeleton-text {
    height: 14px;
  }
}
</style>