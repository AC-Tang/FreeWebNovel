import request from '@/utils/request'

// 文件上传接口
export const uploadApi = {
  // 单文件上传
  uploadFile(file, onUploadProgress) {
    const formData = new FormData()
    formData.append('file', file)

    return request({
      url: '/upload/file',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    })
  },

  // 多文件上传
  uploadMultipleFiles(files, onUploadProgress) {
    const formData = new FormData()
    files.forEach((file) => {
      formData.append('files', file)
    })

    return request({
      url: '/upload/multiple',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress,
    })
  },
}
