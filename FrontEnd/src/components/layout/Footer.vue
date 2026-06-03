<template>
  <footer class="footer">
    <div class="footer-container">
      <div class="footer-content">
        <div class="footer-section">
          <h3>关于我们</h3>
          <ul>
            <li><a href="#">网站介绍</a></li>
            <li><a href="#">联系我们</a></li>
            <li><a href="#">用户协议</a></li>
            <li><a href="#">隐私政策</a></li>
          </ul>
        </div>
        
        <div class="footer-section">
          <h3>帮助中心</h3>
          <ul>
            <li><a href="#">常见问题</a></li>
            <li><a href="#">意见反馈</a></li>
            <li><a href="#">侵权投诉</a></li>
            <li><a href="#">友情链接</a></li>
          </ul>
        </div>
        
        <div class="footer-section">
          <h3>作者服务</h3>
          <ul>
            <li><a href="#">作者福利</a></li>
            <li><a href="#">签约投稿</a></li>
            <li><a href="#">作者后台</a></li>
            <li><a href="#">写作帮助</a></li>
          </ul>
        </div>
        
        <div class="footer-section">
          <h3>关注我们</h3>
          <div class="social-links">
            <a href="#" title="微信"><el-icon><ChatDotRound /></el-icon></a>
            <a href="#" title="微博"><el-icon><Share /></el-icon></a>
            <a href="#" title="QQ"><el-icon><Message /></el-icon></a>
            <a href="#" title="邮箱"><el-icon><Message /></el-icon></a>
          </div>
          <div class="app-download">
            <p>下载APP</p>
            <div class="qr-code">
              <!-- 这里可以放置二维码图片 -->
              <el-icon size="80"><QrCode /></el-icon>
            </div>
          </div>
        </div>
      </div>
      
      <div class="footer-bottom">
        <p>Copyright © {{ currentYear }} FreeFiction All Rights Reserved</p>
      </div>
    </div>
  </footer>
</template>

<script setup name="MainFooter">
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.footer {
  background-color: #2c3e50;
  color: #ecf0f1;
  padding: 40px 0 20px;
  margin-top: 40px;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
}

.footer-section {
  flex: 1;
  min-width: 200px;
  margin-bottom: 30px;
  padding-right: 20px;
}

.footer-section h3 {
  font-size: 18px;
  margin-bottom: 15px;
  color: #fff;
  position: relative;
  padding-bottom: 8px;
}

.footer-section h3::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  width: 40px;
  height: 2px;
  background-color: #409eff;
}

.footer-section ul {
  list-style: none;
  padding: 0;
}

.footer-section ul li {
  margin-bottom: 10px;
}

.footer-section ul li a {
  color: #bdc3c7;
  text-decoration: none;
  transition: color 0.3s;
}

.footer-section ul li a:hover {
  color: #409eff;
}

.social-links {
  display: flex;
  margin-bottom: 20px;
}

.social-links a {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.1);
  margin-right: 10px;
  color: #ecf0f1;
  transition: all 0.3s;
}

.social-links a:hover {
  background-color: #409eff;
  transform: translateY(-3px);
}

.app-download p {
  margin-bottom: 10px;
  font-size: 16px;
}

.qr-code {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100px;
  height: 100px;
  background-color: #fff;
  border-radius: 8px;
}

.footer-bottom {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
  margin-top: 20px;
  color: #95a5a6;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-content {
    flex-direction: column;
  }
  
  .footer-section {
    margin-bottom: 25px;
  }
  
  .app-download {
    display: flex;
    align-items: center;
  }
  
  .app-download p {
    margin-right: 15px;
    margin-bottom: 0;
  }
}
</style>