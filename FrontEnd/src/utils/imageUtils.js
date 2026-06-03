/**
 * 图片URL处理工具
 */

/**
 * 处理图片URL，将相对路径转换为完整URL
 * @param {string} imagePath - 图片路径，可以是相对路径或完整URL
 * @param {string} baseUrl - 基础URL，默认为http://localhost:8080
 * @param {string} defaultImage - 默认图片URL，当imagePath为空时使用
 * @returns {string} 处理后的完整图片URL
 */
export const getImageUrl = (imagePath, baseUrl = 'http://localhost:8080', defaultImage = 'https://picsum.photos/seed/default/200/280.jpg') => {
  if (!imagePath) return defaultImage
  // 如果已经是完整URL，直接返回
  if (imagePath.startsWith('http')) return imagePath
  // 否则拼接服务器地址
  return `${baseUrl}${imagePath}`
}