<template>
  <el-dialog
    v-model="visible"
    title="错误提示"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
  >
    <div class="error-content">
      <div class="error-icon">
        <el-icon size="48" color="#f56c6c">
          <CircleCloseFilled />
        </el-icon>
      </div>
      <div class="error-message">{{ message }}</div>
      <div class="error-description" v-if="description">{{ description }}</div>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel" v-if="showCancel">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { CircleCloseFilled } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    default: '操作失败'
  },
  description: {
    type: String,
    default: ''
  },
  showCancel: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = ref(props.modelValue)

watch(() => props.modelValue, (newVal) => {
  visible.value = newVal
})

watch(visible, (newVal) => {
  emit('update:modelValue', newVal)
})

const handleConfirm = () => {
  visible.value = false
  emit('confirm')
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}
</script>

<style scoped>
.error-content {
  text-align: center;
  padding: 20px 0;
}

.error-icon {
  margin-bottom: 15px;
}

.error-message {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 10px;
}

.error-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style>