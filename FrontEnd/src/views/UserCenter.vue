<template>
  <div class="user-center-page">
    <div class="user-center-container">
      <!-- 左侧导航栏 -->
        <div class="user-sidebar">
          <nav class="sidebar-nav">
            <div class="nav-item" :class="{ active: activeMenu === 'profile' }" @click="handleMenuSelect('profile')">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </div>
            <div class="nav-item" :class="{ active: activeMenu === 'history' }" @click="handleMenuSelect('history')">
              <el-icon><Clock /></el-icon>
              <span>最近阅读</span>
            </div>
            <div class="nav-item" :class="{ active: activeMenu === 'bookshelf' }" @click="handleMenuSelect('bookshelf')">
              <el-icon><Collection /></el-icon>
              <span>我的书架</span>
            </div>
            <div class="nav-item" :class="{ active: activeMenu === 'dynamics' }" @click="handleMenuSelect('dynamics')">
              <el-icon><TrendCharts /></el-icon>
              <span>我的动态</span>
            </div>
            <div class="nav-item" :class="{ active: activeMenu === 'messages' }" @click="handleMenuSelect('messages')">
              <el-icon><Message /></el-icon>
              <span>我的消息</span>
              <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount }}</span>
            </div>
            <div class="nav-divider"></div>
            <div class="nav-item" :class="{ active: activeMenu === 'edit-profile' }" @click="handleMenuSelect('edit-profile')">
              <el-icon><Edit /></el-icon>
              <span>编辑个人资料</span>
            </div>
          </nav>
        </div>

      <!-- 右侧内容区 -->
      <div class="user-content">
        <!-- 个人中心 -->
        <template v-if="activeMenu === 'profile'">
          <!-- 第一块：用户信息区域 -->
          <div class="user-info-panel">
            <!-- 头像和用户名 -->
            <div class="profile-header">
              <div class="profile-avatar">
                <img :src="userInfo.avatar || '/images/default-avatar.png'" :alt="userInfo.username" />
              </div>
              <div class="profile-basic-info">
                <h2 class="username">{{ userInfo.username }}</h2>
                <div class="likes-count">获赞：{{ userInfo.likesCount }}</div>
              </div>
            </div>

            <div class="user-signature">{{ userInfo.signature || '简介：这个人潇洒不羁爱自由，没写什么介绍~' }}</div>
          </div>

          <!-- 第二块：卡片区域 -->
          <div class="cards-panel">
            <!-- 我的动态卡片 -->
            <div class="info-card">
              <div class="card-header">
                <h3 class="card-title">我的动态</h3>
              </div>
              <div class="card-body">
                <div class="review-stats-triangle">
                  <!-- 顶部：书评 -->
                  <div class="triangle-top">
                    <div class="stat-number" @click="handleReviewCountClick('bookReview')">{{ bookReviewCount }}</div>
                    <div class="stat-label">书评</div>
                  </div>
                  <!-- 底部：章评和段评 -->
                  <div class="triangle-bottom">
                    <div class="stat-section">
                      <div class="stat-number" @click="handleReviewCountClick('chapterReview')">{{ chapterReviewCount }}</div>
                      <div class="stat-label">章评</div>
                    </div>
                    <div class="stat-section">
                      <div class="stat-number" @click="handleReviewCountClick('paragraphReview')">{{ paragraphReviewCount }}</div>
                      <div class="stat-label">段评</div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="card-footer">
                <el-button type="primary" text @click="handleMenuSelect('dynamics')">
                  查看详情
                </el-button>
              </div>
            </div>

            <!-- 用户互动卡片 -->
            <div class="info-card" @click="handleMenuSelect('messages')">
              <div class="card-header">
                <h3 class="card-title">用户互动</h3>
                <span v-if="interactionUnreadCount > 0" class="unread-badge">{{ interactionUnreadCount }}</span>
              </div>
              <div class="card-body">
                <div class="review-stats-triangle">
                  <!-- 互动消息放在顶部 -->
                  <div class="triangle-top">
                    <div class="stat-number" @click="handleMessageCountClick('interaction')" @mouseenter="highlightNumber" @mouseleave="unhighlightNumber">{{ interactionMessages.length }}</div>
                    <div class="stat-label">互动消息</div>
                  </div>
                  <!-- 系统通知和请求处理呈三角状排列 -->
                  <div class="triangle-bottom">
                    <div class="stat-section">
                      <div class="stat-number" @click="handleMessageCountClick('system')" @mouseenter="highlightNumber" @mouseleave="unhighlightNumber">{{ systemMessages.length }}</div>
                      <div class="stat-label">系统通知</div>
                    </div>
                    <div class="stat-section">
                      <div class="stat-number" @click="handleMessageCountClick('requests')" @mouseenter="highlightNumber" @mouseleave="unhighlightNumber">{{ requestMessages.length }}</div>
                      <div class="stat-label">请求处理</div>
                    </div>
                  </div>
                </div>
              </div>
              <div class="card-footer">
                <el-button type="primary" text @click="handleMenuSelect('messages')">
                  查看详情
                </el-button>
              </div>
            </div>
          </div>
        </template>

        <!-- 编辑个人资料 -->
        <div v-if="activeMenu === 'edit-profile'" class="content-panel">
          <div class="panel-header">
            <h2 class="panel-title">编辑个人资料</h2>
          </div>
          <div class="panel-body">
            <el-tabs v-model="profileTab" class="profile-tabs">
              <el-tab-pane label="基本信息" name="basic">
                <el-form :model="userInfo" label-width="120px" class="edit-form">
                  <!-- 账号信息 -->
                  <el-form-item label="账号">
                    <div class="form-row">
                      <span class="account-value">{{ userInfo.username || '用户账号' }}</span>
                      <el-button type="primary" size="small" plain class="modify-btn" @click="showPasswordDialog = true">
                        修改密码
                      </el-button>
                    </div>
                  </el-form-item>

                  <!-- 用户昵称 -->
                  <el-form-item label="用户昵称">
                    <div class="form-row">
                      <el-input v-model="userInfo.username" placeholder="请输入用户昵称" class="nickname-input" />
                      <span class="limit-text">30天内只能修改一次</span>
                    </div>
                  </el-form-item>

                  <!-- 账号类型 -->
                  <el-form-item label="账号类型">
                    <div class="form-row">
                      <div class="account-type">
                        <el-icon v-if="userInfo.roleName === 'ROLE_ADMIN'" class="admin-icon"><UserFilled /></el-icon>
                        <el-icon v-else-if="userInfo.roleName === 'ROLE_USER'" class="user-icon"><User /></el-icon>
                        <el-icon v-else class="vip-icon"><Star /></el-icon>
                        <span v-if="userInfo.roleName === 'ROLE_ADMIN'">管理员</span>
                        <span v-else-if="userInfo.roleName === 'ROLE_USER'">普通用户</span>
                        <span v-else>升级还需消费 20000 纵横币</span>
                      </div>
                    </div>
                  </el-form-item>

                  <!-- 出生日期 -->
                  <el-form-item label="出生日期">
                    <el-date-picker
                      v-model="userInfo.birthday"
                      type="date"
                      placeholder="选择出生日期"
                      style="width: 100%"
                    />
                  </el-form-item>

                  <!-- 性别 -->
                  <el-form-item label="性别">
                    <el-radio-group v-model="userInfo.gender" class="gender-radio">
                      <el-radio label="unknown">保密</el-radio>
                      <el-radio label="male">男</el-radio>
                      <el-radio label="female">女</el-radio>
                    </el-radio-group>
                  </el-form-item>

                  <!-- 绑定邮箱 -->
                  <el-form-item label="绑定邮箱">
                    <div class="form-row">
                      <span class="phone-value">{{ userInfo.email || '未绑定邮箱' }}</span>
                      <el-button type="primary" size="small" plain class="modify-btn" @click="showEmailDialog = true">
                        修改绑定邮箱
                      </el-button>
                    </div>
                  </el-form-item>

                  <!-- 个人签名 -->
                  <el-form-item label="个人签名" prop="signature" :error="signatureError">
                    <el-input
                      v-model="userInfo.signature"
                      type="textarea"
                      :rows="4"
                      placeholder="请输入个人签名"
                      :maxlength="100"
                      show-word-limit
                      class="signature-textarea"
                      @input="validateSignature"
                    />
                  </el-form-item>

                  <!-- 操作按钮 -->
                  <el-form-item>
                    <div class="form-actions">
                      <el-button class="cancel-btn" @click="resetProfile">取消修改</el-button>
                      <el-button type="primary" class="save-btn" @click="saveProfile" :loading="savingProfile">保存修改</el-button>
                    </div>
                  </el-form-item>
                </el-form>
              </el-tab-pane>

              <el-tab-pane label="修改头像" name="avatar">
                <div class="avatar-edit">
                  <div class="avatar-preview">
                    <img :src="userInfo.avatar || '/images/default-avatar.png'" :alt="userInfo.username" class="current-avatar" />
                  </div>
                  <div class="avatar-upload">
                    <el-upload
                      class="avatar-uploader"
                      action="#"
                      :show-file-list="false"
                      :http-request="handleCustomAvatarUpload"
                      :before-upload="beforeAvatarUpload"
                    >
                      <el-button
                        type="primary"
                        :loading="avatarUploading"
                        :disabled="avatarUploading"
                      >
                        {{ avatarUploading ? '上传中...' : '上传头像' }}
                      </el-button>
                    </el-upload>
                    <div class="upload-tips">
                      <div>建议尺寸：200×200像素</div>
                      <div>支持格式：JPG、PNG、GIF、WebP</div>
                      <div>文件大小：10KB - 5MB</div>
                    </div>
                  </div>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>

        <!-- 我的动态 -->
        <div v-if="activeMenu === 'dynamics'" class="content-panel">
          <div class="panel-body">
            <!-- 导航标签页 -->
            <el-tabs v-model="dynamicsTab" class="dynamics-tabs" @tab-change="handleDynamicsTabChange">
              <el-tab-pane label="书评" name="bookReview">
                <div class="review-list" v-if="bookReviews.length > 0">
                  <div v-for="review in bookReviews" :key="review.id" class="review-item">
                    <div class="review-content" @click="handleReviewClick(review)">
                      <div class="review-text">{{ review.content }}</div>
                      <div class="review-book">《{{ review.bookTitle }}》</div>
                      <div class="review-time">{{ formatTime(review.createTime) }}</div>
                    </div>
                    <div class="review-actions">
                      <el-button
                        size="small"
                        type="danger"
                        text
                        @click.stop="deleteComment(review.id)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-state">
                  <el-icon><Document /></el-icon>
                  <p>暂无书评记录</p>
                </div>
              </el-tab-pane>

              <el-tab-pane label="章评" name="chapterReview">
                <div class="review-list" v-if="chapterReviews.length > 0">
                  <div v-for="review in chapterReviews" :key="review.id" class="review-item">
                    <div class="review-content" @click="handleReviewClick(review)">
                      <div class="review-text">{{ review.content }}</div>
                      <div class="review-book">《{{ review.bookTitle }}》 - {{ review.chapterTitle }}</div>
                      <div class="review-time">{{ formatTime(review.createTime) }}</div>
                    </div>
                    <div class="review-actions">
                      <el-button
                        size="small"
                        type="danger"
                        text
                        @click.stop="deleteComment(review.id)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-state">
                  <el-icon><Document /></el-icon>
                  <p>暂无章评记录</p>
                </div>
              </el-tab-pane>

              <el-tab-pane label="段评" name="paragraphReview">
                <div class="review-list" v-if="paragraphReviews.length > 0">
                  <div v-for="review in paragraphReviews" :key="review.id" class="review-item">
                    <div class="review-content" @click="handleReviewClick(review)">
                      <div class="review-text">{{ review.content }}</div>
                      <div class="review-book">《{{ review.bookTitle }}》 - {{ review.chapterTitle }}</div>
                      <div class="review-time">{{ formatTime(review.createTime) }}</div>
                    </div>
                    <div class="review-actions">
                      <el-button
                        size="small"
                        type="danger"
                        text
                        @click.stop="deleteComment(review.id)"
                      >
                        删除
                      </el-button>
                    </div>
                  </div>
                </div>
                <div v-else class="empty-state">
                  <el-icon><Document /></el-icon>
                  <p>暂无段评记录</p>
                </div>
              </el-tab-pane>
            </el-tabs>
          </div>
        </div>

        <!-- 我的消息 -->
        <div v-if="activeMenu === 'messages'" class="content-panel">
          <div class="panel-body">
            <div class="message-tabs-header">
              <el-tabs v-model="messageTab" class="message-tabs" @tab-change="handleMessageTabChange">
                <el-tab-pane label="系统通知" name="system">
                  <div class="message-list" v-if="systemMessages.length > 0">
                    <div
                      v-for="message in systemMessages"
                      :key="message.id"
                      class="message-item"
                      :class="{ unread: !message.isRead }"
                    >
                      <div class="message-icon">
                        <el-icon><Bell /></el-icon>
                      </div>
                      <div class="message-content">
                        <div class="message-title">{{ message.title }}</div>
                        <div class="message-text">{{ message.content }}</div>
                        <div class="message-time">{{ formatTime(message.createTime) }}</div>
                      </div>
                      <div class="message-actions">
                        <el-button
                          size="small"
                          text
                          @click="markNotificationAsRead(message.id, 'system')"
                          v-if="!message.isRead"
                        >
                          已读
                        </el-button>
                        <el-button
                          size="small"
                          text
                          @click="deleteNotification(message.id, 'system')"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-state">
                    <el-icon><Bell /></el-icon>
                    <p>暂无系统通知</p>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="互动消息" name="interaction">
                  <div class="message-list" v-if="interactionMessages.length > 0">
                    <div
                      v-for="message in interactionMessages"
                      :key="message.id"
                      class="message-item"
                      :class="{ unread: !message.isRead }"
                    >
                      <div class="message-icon">
                        <el-icon><ChatDotRound /></el-icon>
                      </div>
                      <div class="message-content">
                        <div class="message-title">{{ message.title }}</div>
                        <div class="message-text">{{ message.content }}</div>
                        <div class="message-time">{{ formatTime(message.createTime) }}</div>
                      </div>
                      <div class="message-actions">
                        <el-button
                          size="small"
                          text
                          @click="markNotificationAsRead(message.id, 'interaction')"
                          v-if="!message.isRead"
                        >
                          已读
                        </el-button>
                        <el-button
                          size="small"
                          text
                          @click="deleteNotification(message.id, 'interaction')"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-state">
                    <el-icon><ChatDotRound /></el-icon>
                    <p>暂无互动消息</p>
                  </div>
                </el-tab-pane>
                <el-tab-pane label="请求处理" name="requests">
                  <div class="message-list" v-if="requestMessages.length > 0">
                    <div
                      v-for="message in requestMessages"
                      :key="message.id"
                      class="message-item"
                      :class="{ unread: !message.isRead }"
                    >
                      <div class="message-icon">
                        <el-icon><Warning /></el-icon>
                      </div>
                      <div class="message-content">
                        <div class="message-title">{{ message.title }}</div>
                        <div class="message-text">{{ message.content }}</div>
                        <div class="message-time">{{ formatTime(message.createTime) }}</div>
                      </div>
                      <div class="message-actions">
                        <el-button
                          size="small"
                          text
                          @click="markNotificationAsRead(message.id, 'request')"
                          v-if="!message.isRead"
                        >
                          已读
                        </el-button>
                        <el-button
                          size="small"
                          text
                          @click="deleteNotification(message.id, 'request')"
                        >
                          删除
                        </el-button>
                      </div>
                    </div>
                  </div>
                  <div v-else class="empty-state">
                    <el-icon><Warning /></el-icon>
                    <p>暂无请求处理消息</p>
                  </div>
                </el-tab-pane>
              </el-tabs>
              <div class="tabs-actions">
                <div class="action-button" @click="markAllAsRead" :class="{ disabled: unreadCount === 0 }">
                      全部已读
                </div>
                <div class="action-button danger" @click="clearAllMessages">
                      一键清除
                </div>
                <div class="message-stats">
                  <span class="total-count">
                    系统通知: {{ systemMessages.length }} |
                    互动消息: {{ interactionMessages.length }} |
                    请求处理: {{ requestMessages.length }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

          <!-- 账号设置 -->
        <div v-if="activeMenu === 'settings'" class="content-panel">
          <div class="panel-header">
            <h2 class="panel-title">账号设置</h2>
          </div>
          <div class="panel-body">
            <div class="settings-section">
              <h3 class="section-subtitle">账号安全</h3>
              <div class="setting-item">
                <div class="setting-info">
                  <span class="setting-label">修改密码</span>
                  <span class="setting-desc">定期更换密码保护账号安全</span>
                </div>
                <el-button type="primary" size="small" @click="showPasswordDialog = true">修改</el-button>
              </div>
              <div class="setting-item">
                <div class="setting-info">
                  <span class="setting-label">绑定邮箱</span>
                  <span class="setting-desc">{{ userInfo.email || '未绑定，绑定后可用于找回密码' }}</span>
                </div>
                <el-button v-if="!userInfo.email" type="primary" size="small">绑定</el-button>
                <el-button v-else size="small">修改</el-button>
              </div>
              <div class="setting-item">
                <div class="setting-info">
                  <span class="setting-label">绑定手机</span>
                  <span class="setting-desc">{{ userInfo.phone || '未绑定，绑定后可用于找回密码' }}</span>
                </div>
                <el-button v-if="!userInfo.phone" type="primary" size="small">绑定</el-button>
                <el-button v-else size="small">修改</el-button>
              </div>
            </div>

            <div class="settings-section">
              <h3 class="section-subtitle">隐私设置</h3>
              <div class="setting-item">
                <div class="setting-info">
                  <span class="setting-label">阅读记录可见</span>
                  <span class="setting-desc">其他用户可以看到您的阅读记录</span>
                </div>
                <el-switch v-model="privacySettings.readingVisible" />
              </div>
              <div class="setting-item">
                <div class="setting-info">
                  <span class="setting-label">接收推荐</span>
                  <span class="setting-desc">接收系统推荐的书籍</span>
                </div>
                <el-switch v-model="privacySettings.receiveRecommend" />
              </div>
            </div>

            <div class="settings-section danger-zone">
              <h3 class="section-subtitle">危险操作</h3>
              <div class="setting-item">
                <div class="setting-info">
                  <span class="setting-label">注销账号</span>
                  <span class="setting-desc">此操作不可恢复，请谨慎操作</span>
                </div>
                <el-button type="danger" size="small" @click="showDeleteDialog = true">注销</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 修改密码弹窗 -->
  <el-dialog
    v-model="showPasswordDialog"
    title="修改密码"
    width="400px"
    :close-on-click-modal="false"
    @closed="handlePasswordDialogClosed"
  >
    <el-form
      ref="passwordFormRef"
      :model="passwordForm"
      :rules="passwordRules"
      label-width="80px"
    >
      <el-form-item label="旧密码" prop="currentPassword">
        <el-input
          v-model="passwordForm.currentPassword"
          type="password"
          placeholder="请输入旧密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          type="password"
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          type="password"
          placeholder="请再次输入新密码"
          show-password
          class="confirm-password-input"
        />
      </el-form-item>

      <el-form-item label="验证码" prop="captcha">
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-input
            v-model="passwordForm.captcha"
            placeholder="请输入验证码"
            style="flex: 1;"
          />
          <img
            v-if="captchaImage"
            :src="captchaImage"
            @click="getCaptcha"
            style="height: 40px; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px;"
            title="点击刷新验证码"
          />
          <el-button
            v-else
            @click="getCaptcha"
            :loading="sendingCaptcha"
            size="small"
          >
            获取验证码
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="changePassword">确定</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- 修改邮箱弹窗 -->
  <el-dialog
    v-model="showEmailDialog"
    title="修改绑定邮箱"
    width="400px"
    :close-on-click-modal="false"
    @closed="handleEmailDialogClosed"
  >
    <el-form
      ref="emailFormRef"
      :model="emailForm"
      :rules="emailRules"
      label-width="80px"
    >
      <el-form-item label="新邮箱" prop="newEmail">
        <el-input
          v-model="emailForm.newEmail"
          placeholder="请输入新邮箱"
          clearable
        />
      </el-form-item>

      <el-form-item label="邮箱验证码" prop="emailCode">
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-input
            v-model="emailForm.emailCode"
            placeholder="请输入邮箱验证码"
            style="flex: 1;"
          />
          <el-button
            type="primary"
            @click="handleSendEmailCode"
            :disabled="emailCountdown > 0"
            :loading="sendingCaptcha"
            size="small"
          >
            {{ emailCountdown > 0 ? `${emailCountdown}秒后重发` : '发送验证码' }}
          </el-button>
        </div>
      </el-form-item>

      <el-form-item label="图形验证码" prop="captcha">
        <div style="display: flex; align-items: center; gap: 10px;">
          <el-input
            v-model="emailForm.captcha"
            placeholder="请输入图形验证码"
            style="flex: 1;"
          />
          <img
            v-if="captchaImage"
            :src="captchaImage"
            @click="getCaptcha"
            style="height: 40px; cursor: pointer; border: 1px solid #dcdfe6; border-radius: 4px;"
            title="点击刷新验证码"
          />
          <el-button
            v-else
            @click="getCaptcha"
            :loading="sendingCaptcha"
            size="small"
          >
            获取验证码
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="showEmailDialog = false">取消</el-button>
        <el-button type="primary" @click="changeEmail">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup name="UserCenterPage">
import { ref, onMounted, watch, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@/api/Users'
import { notificationApi } from '@/api/Notification'
import { commentsApi } from '@/api/Comment'
import { User, Collection, Clock, Star, Message, Bell, Document, Edit, TrendCharts, ChatDotRound, Warning, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()

// 当前激活的菜单
const activeMenu = ref('profile')
const messageTab = ref('system')
const savingProfile = ref(false)
const dynamicsTab = ref('bookReview') // 我的动态页面默认显示书评
const profileTab = ref('basic') // 编辑个人资料默认显示基本信息标签页
const signatureError = ref('') // 个人签名错误信息

// 用户信息
const userInfo = ref({
  id: '',
  username: '',
  email: '',
  gender: 'unknown',
  birthday: null,
  signature: '',
  avatar: '',
  roleName: '',
  createTime: '',
  likesCount: 0
})

// 用户统计数据
const userLikes = ref(128)

// 评论统计数据
const bookReviewCount = ref(0)
const chapterReviewCount = ref(0)
const paragraphReviewCount = ref(0)

const privacySettings = ref({
  readingVisible: true,
  receiveRecommend: true
})

// 未读消息数量
const unreadCount = ref(3)

// 数字高亮状态 - 已集成到交互中

// 计算属性
const interactionUnreadCount = computed(() => {
  return interactionMessages.value.filter(msg => !msg.isRead).length
})


// 评论数据
const bookReviews = ref([])
const chapterReviews = ref([])
const paragraphReviews = ref([])

// 消息列表
const systemMessages = ref([])
const interactionMessages = ref([])
const requestMessages = ref([])

// 修改密码表单
const passwordForm = ref({
  currentPassword: '',
  newPassword: '',
  confirmPassword: '',
  captcha: ''
})

// 修改邮箱表单
const emailForm = ref({
  newEmail: '',
  emailCode: '',
  captcha: ''
})

// 表单引用
const passwordFormRef = ref(null)
const emailFormRef = ref(null)

// 表单验证规则
const passwordRules = reactive({
  currentPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (value === passwordForm.value.currentPassword) {
          callback(new Error('新密码不能与旧密码相同'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度不正确', trigger: 'blur' }
  ]
})

// 邮箱验证规则
const emailRules = reactive({
  newEmail: [
    { required: true, message: '请输入新邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
    { pattern: /^[1-9]\d{4,10}@qq\.com$/, message: '请输入有效的QQ邮箱', trigger: 'blur' }
  ],
  emailCode: [
    { required: true, message: '请输入邮箱验证码', trigger: 'blur' },
    { min: 6, max: 6, message: '验证码长度为6位', trigger: 'blur' }
  ],
  captcha: [
    { required: true, message: '请输入图形验证码', trigger: 'blur' },
    { min: 4, max: 6, message: '验证码长度不正确', trigger: 'blur' }
  ]
})

// 验证码相关
const captchaImage = ref('')
const captchaKey = ref('')
const sendingCaptcha = ref(false)
const emailCountdown = ref(0)




// 对话框状态
const showPasswordDialog = ref(false)
const showDeleteDialog = ref(false)
const showEmailDialog = ref(false)

// 数字高亮处理函数
const highlightNumber = (event) => {
  const numberElement = event.target
  numberElement.style.color = '#f56c6c'
  numberElement.style.transform = 'scale(1.1)'
  numberElement.style.transition = 'all 0.3s ease'
}

const unhighlightNumber = (event) => {
  const numberElement = event.target
  numberElement.style.color = '#333'
  numberElement.style.transform = 'scale(1)'
}

// 消息数量点击处理函数
const handleMessageCountClick = (type) => {
  // 设置当前激活的菜单为消息
  activeMenu.value = 'messages'

  // 根据类型设置消息标签页
  switch (type) {
    case 'interaction':
      messageTab.value = 'interaction'
      break
    case 'system':
      messageTab.value = 'system'
      break
    case 'requests':
      messageTab.value = 'requests'
      break
    default:
      messageTab.value = 'system'
  }
}

// 发送邮箱验证码
const handleSendEmailCode = async () => {
  try {
    // 验证新邮箱格式
    await emailFormRef.value.validateField('newEmail')

    // 显示加载状态
    sendingCaptcha.value = true

    // 调用发送邮箱验证码接口
    await userApi.getEmailCaptcha({ email: emailForm.value.newEmail })

    // 发送成功
    ElMessage.success('验证码发送成功，请查收邮箱')

    // 开始倒计时
    startEmailCountdown()
  } catch (error) {
    // 发送失败处理
    console.error('发送验证码失败:', error)
    if (error.response) {
      ElMessage.error(error.response.data.message || '发送验证码失败')
    } else {
      ElMessage.error('发送验证码失败，请稍后重试')
    }
  } finally {
    // 隐藏加载状态
    sendingCaptcha.value = false
  }
}

// 邮箱验证码倒计时
const startEmailCountdown = () => {
  emailCountdown.value = 60
  const timer = setInterval(() => {
    emailCountdown.value--
    if (emailCountdown.value <= 0) {
      clearInterval(timer)
    }
  }, 1000)
}

// 修改邮箱
const changeEmail = async () => {
  try {
    // 1. 验证整个表单
    await emailFormRef.value.validate()

    // 2. 验证图形验证码（不区分大小写）
    try {
      await userApi.verifyCaptcha({
        captcha: emailForm.value.captcha.toLowerCase(),
        captchaId: captchaKey.value
      })
    } catch (error) {
      console.error('图形验证码验证失败:', error)
      // 重新获取验证码
      getCaptcha()
      // 清空验证码输入
      emailForm.value.captcha = ''
      return
    }

    // 3. 验证邮箱验证码
    try {
      await userApi.verifyEmailCaptcha({
        email: emailForm.value.newEmail,
        code: emailForm.value.emailCode
      })
    } catch (error) {
      console.error('邮箱验证码验证失败:', error)
      ElMessage.error('邮箱验证码错误或已过期')
      // 重新获取图形验证码
      getCaptcha()
      emailForm.value.captcha = ''
      return
    }

    // 4. 修改邮箱
    const response = await userApi.changeEmail({
      email: emailForm.value.newEmail,
      id: userInfo.value.id
    })

    // 5. 处理返回结果
    if (response.code === 200) {
      ElMessage.success('邮箱修改成功')
      showEmailDialog.value = false
      // 更新用户信息
      userInfo.value.email = emailForm.value.newEmail
      // 重置表单
      emailForm.value = {
        newEmail: '',
        emailCode: '',
        captcha: ''
      }
      captchaImage.value = ''
      captchaKey.value = ''
      emailCountdown.value = 0
    } else {
      ElMessage.error(response.message || '邮箱修改失败')
      // 重新获取验证码
      getCaptcha()
      emailForm.value.captcha = ''
    }
  } catch (error) {
    // 表单验证失败
    if (error !== false) {
      ElMessage.error('请检查输入内容')
    }
    console.warn('邮箱修改表单验证失败:', error)
  }
}

// 获取用户评论数据
const fetchUserComments = async () => {
  try {
    if (!userInfo.value.id) {
      return
    }

    // 获取用户所有评论
    const response = await commentsApi.getUserComments(userInfo.value.id)

    if (response.code === 200 && response.data) {
      const comments = response.data

      // 清空现有数据
      bookReviews.value = []
      chapterReviews.value = []
      paragraphReviews.value = []

      // 分类处理评论数据
      comments.forEach(comment => {
        const reviewData = {
          id: comment.id,
          content: comment.content || '',
          bookTitle: comment.bookName|| '未知书籍',
          bookId: comment.bookId,
          createTime: new Date(comment.createdAt || Date.now())
        }

        switch (comment.commentType) {
          case 1: // 章评
            chapterReviews.value.push({
              ...reviewData,
              chapterTitle: comment.chapterName || `第${comment.chapterId || 1}章`,
              realChapterId: comment.realChapterId
            })
            break
          case 2: // 段评
            paragraphReviews.value.push({
              ...reviewData,
              chapterTitle: comment.chapterName|| `第${comment.chapterId || 1}章`,
              realChapterId: comment.realChapterId
            })
            break
          case 3: // 书评
            bookReviews.value.push(reviewData)
            break
          default:
            // 根据评论内容判断类型
            if (comment.content && comment.content.length > 100) {
              bookReviews.value.push(reviewData) // 长评论视为书评
            } else if (comment.chapterId) {
              chapterReviews.value.push({
                ...reviewData,
                chapterTitle: comment.chapterTitle || `第${comment.chapterId}章`
              }) // 有章节ID的视为章评
            } else {
              paragraphReviews.value.push({
                ...reviewData,
                chapterTitle: comment.chapterTitle || `第${comment.chapterId || 1}章`
              }) // 其他视为段评
            }
        }
      })
      // 更新评论数量统计
      bookReviewCount.value = bookReviews.value.length
      chapterReviewCount.value = chapterReviews.value.length
      paragraphReviewCount.value = paragraphReviews.value.length

    }
  } catch (error) {
    console.error('获取评论数据失败:', error)
    // 如果API失败，使用模拟数据
  }
}

// 删除评论
const deleteComment = async (id) => {
  try {
    // 显示确认弹窗
    await ElMessageBox.confirm('确定要删除这条评论吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 用户确认后执行删除操作
    const response = await commentsApi.deleteComment(id)
    if (response.code === 200) {
      ElMessage.success('评论删除成功')
      // 重新获取评论数据
      await fetchUserComments()
    } else {
      ElMessage.error(response.message || '删除评论失败')
    }
  } catch (error) {
    // 如果用户取消了确认，不显示错误信息
    if (error !== 'cancel') {
      ElMessage.error('删除评论失败')
      console.warn('删除评论失败:', error)
    }
  }
}

// 处理评论点击跳转
const handleReviewClick = (review) => {
  if (review.realChapterId) {
    // 有realChapterId且不为空，跳转到对应章节阅读页
    router.push(`/read/${review.bookId}/${review.realChapterId}`)
  } else {
    // 否则跳转到书籍详情页
    router.push(`/book/${review.bookId}`)
  }
}


// 获取用户通知消息
const fetchUserNotifications = async () => {
  try {
    if (!userInfo.value.id) {
      return
    }

    // 获取未读通知数量
    const unreadResponse = await notificationApi.getUnreadNotificationCount(userInfo.value.id)

    if (unreadResponse.code === 200 && unreadResponse.data !== undefined) {
      unreadCount.value = unreadResponse.data
    } else {
      unreadCount.value = 0
    }

    // 获取不同类型的通知
    const [systemResponse, interactionResponse, requestResponse] = await Promise.all([
      notificationApi.getUserNotificationsByType(userInfo.value.id, 1), // 系统通知
      notificationApi.getUserNotificationsByType(userInfo.value.id, 2), // 互动通知
      notificationApi.getUserNotificationsByType(userInfo.value.id, 3)  // 请求通知
    ])

    // 处理系统通知
    if (systemResponse.code === 200 && systemResponse.data) {
      systemMessages.value = systemResponse.data.map(notification => ({
        id: notification.id || `sys_${Date.now()}_${Math.random()}`,
        title: notification.title || '系统通知',
        content: notification.content || '',
        createTime: new Date(notification.createTime || Date.now()),
        isRead: notification.isRead || false,
        type: 'system'
      }))
    } else {
      systemMessages.value = []
    }

    // 处理互动通知
    if (interactionResponse.code === 200 && interactionResponse.data) {
      interactionMessages.value = interactionResponse.data.map(notification => ({
        id: notification.id || `inter_${Date.now()}_${Math.random()}`,
        title: notification.title || '互动消息',
        content: notification.content || '',
        createTime: new Date(notification.createTime || Date.now()),
        isRead: notification.isRead || false,
        type: 'interaction'
      }))
    } else {
      interactionMessages.value = []
    }

    // 处理请求通知
    if (requestResponse.code === 200 && requestResponse.data) {
      requestMessages.value = requestResponse.data.map(notification => ({
        id: notification.id || `request_${Date.now()}_${Math.random()}`,
        title: notification.title || '请求处理',
        content: notification.content || '',
        createTime: new Date(notification.createTime || Date.now()),
        isRead: notification.isRead || false,
        type: 'request',
        status: notification.status || 'pending'
      }))
    } else {
      requestMessages.value = []
    }
  } catch (error) {
    console.error('获取用户通知失败:', error)
  }
}

// 标记通知为已读
const markNotificationAsRead = async (notificationId, type) => {
  try {
    const response = await notificationApi.markAsRead(notificationId)
    if (response.code === 200) {
      // 更新本地数据
      if (type === 'system') {
        const notification = systemMessages.value.find(n => n.id === notificationId)
        if (notification) notification.isRead = true
      } else if (type === 'interaction') {
        const notification = interactionMessages.value.find(n => n.id === notificationId)
        if (notification) notification.isRead = true
      } else if (type === 'request') {
        const notification = requestMessages.value.find(n => n.id === notificationId)
        if (notification) notification.isRead = true
      }

      // 更新未读数量
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      ElMessage.success('已标记为已读')
    } else {
      ElMessage.error(response.message || '标记已读失败')
    }
  } catch (error) {
    ElMessage.error('标记已读失败')
    console.warn('标记已读失败:', error)
  }
}

// 删除通知
const deleteNotification = async (notificationId, type) => {
  try {
    const response = await notificationApi.deleteNotification(notificationId)
    if (response.code === 200) {
      // 从本地数据中删除
      if (type === 'system') {
        const index = systemMessages.value.findIndex(n => n.id === notificationId)
        if (index > -1) {
          const notification = systemMessages.value[index]
          if (!notification.isRead) unreadCount.value = Math.max(0, unreadCount.value - 1)
          systemMessages.value.splice(index, 1)
        }
      } else if (type === 'interaction') {
        const index = interactionMessages.value.findIndex(n => n.id === notificationId)
        if (index > -1) {
          const notification = interactionMessages.value[index]
          if (!notification.isRead) unreadCount.value = Math.max(0, unreadCount.value - 1)
          interactionMessages.value.splice(index, 1)
        }
      } else if (type === 'request') {
        const index = requestMessages.value.findIndex(n => n.id === notificationId)
        if (index > -1) {
          const notification = requestMessages.value[index]
          if (!notification.isRead) unreadCount.value = Math.max(0, unreadCount.value - 1)
          requestMessages.value.splice(index, 1)
        }
      }

      ElMessage.success('删除成功')
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
    console.warn('删除通知失败:', error)
  }
}

// 标记所有通知为已读
const markAllAsRead = async () => {
  try {
    if (!userInfo.value.id) {
      ElMessage.warning('用户ID不存在')
      return
    }

    const response = await notificationApi.markAllAsRead(userInfo.value.id)
    if (response.code === 200) {
      // 更新所有本地数据为已读
      systemMessages.value.forEach(notification => notification.isRead = true)
      interactionMessages.value.forEach(notification => notification.isRead = true)
      requestMessages.value.forEach(notification => notification.isRead = true)

      // 重置未读数量
      unreadCount.value = 0
      ElMessage.success('全部标记为已读')
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    // 如果API失败，本地标记所有为已读
    systemMessages.value.forEach(notification => notification.isRead = true)
    interactionMessages.value.forEach(notification => notification.isRead = true)
    requestMessages.value.forEach(notification => notification.isRead = true)
    unreadCount.value = 0
    ElMessage.success('全部标记为已读')
    console.warn('标记全部已读API失败，使用本地处理:', error)
  }
}



// 获取用户评论统计（现在使用fetchUserComments获取的数据）
const fetchUserCommentStats = () => {
  // 评论统计现在由fetchUserComments函数负责
  // 这个函数保留用于向后兼容
}

// 获取用户信息
const fetchUserInfo = async () => {
  try {
    const response = await userApi.getUserInfo()

    if (response.code === 200 && response.data) {
      const data = response.data

      // 处理头像数据 - 支持base64字符串和blob对象
      let avatarUrl = data.avatar || ''

      if (data.avatar) {
        // 如果是base64字符串（通常以/9j/开头，这是JPEG的base64特征）
        if (typeof data.avatar === 'string') {
          // 检查是否为base64编码的图片数据
          if (data.avatar.startsWith('/9j/') ||
              data.avatar.startsWith('iVBORw0KGgo') ||
              data.avatar.startsWith('R0lGODlh') ||
              data.avatar.startsWith('UklGR') ||
              data.avatar.startsWith('data:image')) {

            // 如果没有data URI前缀，添加它
            if (!data.avatar.startsWith('data:image')) {
              // 根据内容判断图片类型
              let imageType = 'jpeg'
              if (data.avatar.startsWith('iVBORw0KGgo')) imageType = 'png'
              else if (data.avatar.startsWith('R0lGODlh')) imageType = 'gif'
              else if (data.avatar.startsWith('UklGR')) imageType = 'webp'

              avatarUrl = `data:image/${imageType};base64,${data.avatar}`
            } else {
              avatarUrl = data.avatar
            }
          } else {
            // 可能是普通的URL路径或相对路径
            avatarUrl = data.avatar
          }
        } else if (typeof data.avatar === 'object' && data.avatar instanceof Blob) {
          // 如果是blob对象，转换为URL
          avatarUrl = URL.createObjectURL(data.avatar)
        }
      }

      userInfo.value = {
        id: data.id || '',
        username: data.username || '',
        email: data.email || '',
        gender: data.gender === 1 ? 'male' : data.gender === 2 ? 'female' : 'unknown',
        birthday: data.birthday ? new Date(data.birthday) : null,
        signature: data.signature || data.signature || '', // 兼容旧字段signature
        avatar: avatarUrl,
        roleName: data.roleName || '',
        createTime: data.createTime || '',
        likesCount: data.likesCount || 0,
      }

      // 更新用户统计数据
      if (data.likesCount !== undefined) userLikes.value = data.likesCount

    } else {
      ElMessage.error('获取用户信息失败')
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
    console.warn('获取用户信息失败:', error)
  }
}

// 获取用户数据
const fetchUserData = async () => {
  try {
    // 先获取用户信息
    await fetchUserInfo()

    // 获取用户评论统计数据（已废弃，由fetchUserComments处理）
    fetchUserCommentStats()

    // 获取用户通知消息
    await fetchUserNotifications()

    // 获取用户评论数据
    await fetchUserComments()
  } catch (error) {
    ElMessage.error('获取用户数据失败')
    console.warn('获取用户数据失败:', error)
  }
}

// 跳转到阅读历史页面 - 功能已集成到菜单中

// 处理菜单选择
const handleMenuSelect = (index) => {
  // 跳转到专门的书架页面
  if (index === 'bookshelf') {
    router.push('/bookshelf')
    return
  }

  // 跳转到专门的阅读历史页面
  if (index === 'history') {
    router.push('/reading-history')
    return
  }

  activeMenu.value = index
  // 每次点击"我的动态"时，重置为显示"书评"标签页
  if (index === 'dynamics') {
    dynamicsTab.value = 'bookReview'
  }
  // 每次点击"我的消息"时，重置为显示"系统通知"标签页
  if (index === 'messages') {
    messageTab.value = 'system'
  }
}

// 点击评论统计数字跳转到对应标签页
const handleReviewCountClick = (reviewType) => {
  // 切换到"我的动态"菜单
  activeMenu.value = 'dynamics'
  // 设置对应的标签页
  dynamicsTab.value = reviewType
}

// 获取动态图标 - 功能已集成到组件中

// 跳转到书店 - 功能已移除
// const goToBookstore = () => {
//   router.push('/bookstore')
// }

// 清空所有历史 - 功能已移除
// const clearAllHistory = () => {
//   ElMessageBox.confirm('确定要清空所有阅读历史吗？', '提示', {
//     confirmButtonText: '确定',
//     cancelButtonText: '取消',
//     type: 'warning'
//   }).then(() => {
//     readingHistory.value = []
//     ElMessage.success('阅读历史已清空')
//   }).catch(() => {})
// }



// 处理书架标签页切换 - 功能已集成到标签组件中

// 处理动态标签页切换
const handleDynamicsTabChange = (tabName) => {
  dynamicsTab.value = tabName
}

// 保存个人资料
const saveProfile = async () => {
  try {
    savingProfile.value = true

    // 准备要更新的用户数据
    const updateData = {
      id: userInfo.value.id,
      username: userInfo.value.username,
      signature: userInfo.value.signature,
      birthday: userInfo.value.birthday,
      gender: userInfo.value.gender === 'male' ? 1 : userInfo.value.gender === 'female' ? 2 : 0,
      email: userInfo.value.email
    }

    // 调用API更新用户信息
    const response = await userApi.updateUserInfo(updateData)

    if (response && response.code === 200) {
      ElMessage.success('个人资料保存成功！')
      activeMenu.value = 'profile'
      // 重新获取用户数据以更新显示
      await fetchUserData()
    } else {
      ElMessage.error(response?.message || '保存失败，请稍后重试')
    }
  } catch (error) {
    console.error('保存个人资料失败:', error)
    ElMessage.error(error.message || '保存失败，请检查网络连接')
  } finally {
    savingProfile.value = false
  }
}

// 验证个人签名
const validateSignature = () => {
  if (!userInfo.value.signature) {
    signatureError.value = '请输入个人签名'
  } else {
    signatureError.value = ''
  }
}

// 处理头像上传成功
const handleAvatarSuccess = (response, file) => {
  if (response && response.code === 200) {
    // 处理后端返回的头像数据
    if (response.data?.avatar) {
      // 如果后端返回的是base64数据
      const avatarData = response.data.avatar
      if (typeof avatarData === 'string') {
        // 检查是否为base64编码的图片数据
        if (avatarData.startsWith('/9j/') ||
            avatarData.startsWith('iVBORw0KGgo') ||
            avatarData.startsWith('R0lGODlh') ||
            avatarData.startsWith('UklGR') ||
            avatarData.startsWith('data:image')) {
          // 如果没有data URI前缀，添加它
          if (!avatarData.startsWith('data:image')) {
            let imageType = 'jpeg'
            if (avatarData.startsWith('iVBORw0KGgo')) imageType = 'png'
            else if (avatarData.startsWith('R0lGODlh')) imageType = 'gif'
            else if (avatarData.startsWith('UklGR')) imageType = 'webp'

            userInfo.value.avatar = `data:image/${imageType};base64,${avatarData}`
          } else {
            userInfo.value.avatar = avatarData
          }
        } else {
          // 可能是普通的URL路径或相对路径
          userInfo.value.avatar = avatarData
        }
      } else {
        userInfo.value.avatar = avatarData
      }
    } else if (response.data?.avatarUrl) {
      // 如果后端返回的是URL
      userInfo.value.avatar = response.data.avatarUrl
    } else {
      // 使用本地预览URL作为后备
      userInfo.value.avatar = URL.createObjectURL(file.raw)
    }
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response?.message || '头像上传失败')
  }
}

// 头像上传状态
const avatarUploading = ref(false)

// 自定义头像上传方法
const handleCustomAvatarUpload = async ({ file }) => {
  try {
    avatarUploading.value = true

    // 显示上传进度 - 功能已集成到上传组件中

    // 调用API上传头像
    const response = await userApi.changeAvatar(file, userInfo.value.id)

    if (response && response.code === 200) {
      // 处理上传成功
      handleAvatarSuccess(response, { raw: file })
    } else {
      throw new Error(response?.message || '头像上传失败')
    }

  } catch (error) {
    ElMessage.error(error.message || '头像上传失败，请稍后重试')
  } finally {
    avatarUploading.value = false
  }
}

// 头像上传前的校验
const beforeAvatarUpload = (file) => {
  // 支持的图片格式
  const supportedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  const isValidType = supportedTypes.includes(file.type)

  // 文件大小限制 (5MB)
  const maxSize = 5 * 1024 * 1024 // 5MB
  const isLt5M = file.size <= maxSize

  // 文件最小尺寸限制 (10KB)
  const minSize = 10 * 1024 // 10KB
  const isGt10K = file.size >= minSize

  if (!isValidType) {
    ElMessage.error('头像只能是 JPG/JPEG/PNG/GIF/WebP 格式')
    return false
  }
  if (!isGt10K) {
    ElMessage.error('头像文件大小不能小于 10KB')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('头像大小不能超过 5MB')
    return false
  }

  // 图片尺寸验证
  return new Promise((resolve) => {
    const reader = new FileReader()
    reader.onload = (e) => {
      const img = new Image()
      img.onload = () => {
        const { width, height } = img

        // 建议尺寸检查 (不强制，只提示)
        if (width < 100 || height < 100) {
          ElMessage.warning('头像尺寸较小，可能影响显示效果，建议上传200x200像素以上的图片')
        }

        // 长宽比检查
        const aspectRatio = width / height
        if (aspectRatio < 0.5 || aspectRatio > 2) {
          ElMessage.warning('头像长宽比异常，可能导致显示变形')
        }

        resolve(true)
      }
      img.onerror = () => {
        ElMessage.error('图片文件损坏或无法读取')
        resolve(false)
      }
      img.src = e.target.result
    }
    reader.onerror = () => {
      ElMessage.error('文件读取失败')
      resolve(false)
    }
    reader.readAsDataURL(file)
  })
}

// 从书架移除 - 功能已移除
// const removeFromBookshelf = (bookId) => {
//   ElMessage.success('已从书架移除')
//   // 实际项目中应调用API删除
//   if (bookshelfTab.value === 'reading') {
//     readingBooks.value = readingBooks.value.filter(book => book.id !== bookId)
//   } else {
//     completedBooks.value = completedBooks.value.filter(book => book.id !== bookId)
//   }
// }



// 获取验证码
const getCaptcha = async () => {
  try {
    const response = await userApi.getCaptcha()
    const blob = new Blob([response.data], { type: 'image/jpeg' })
    captchaImage.value = URL.createObjectURL(blob)

    // 从响应头中获取验证码key
    captchaKey.value = response.headers['captcha-key'] || response.headers['x-captcha-id'] || Date.now().toString()
  } catch (error) {
    ElMessage.error('获取验证码失败')
    console.warn('获取验证码失败:', error)
  }
}

// 修改密码
const changePassword = async () => {
  try {
    // 1. 先验证整个表单
    await passwordFormRef.value.validate()

    // 2. 验证新密码和确认密码是否相同
    if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
      ElMessage.error('两次输入的密码不一致')
      return
    }

    // 3. 验证验证码（不区分大小写）
    try {
      await userApi.verifyCaptcha({
        captcha: passwordForm.value.captcha.toLowerCase(), // 验证码不区分大小写
        captchaId: captchaKey.value
      })
    } catch (error) {
      console.error('图形验证码验证失败:', error)
      // 重新获取验证码
      getCaptcha()
      // 清空验证码输入
      passwordForm.value.captcha = ''
      return
    }

    // 4. 修改密码
    const response = await userApi.changePassword({
      email: userInfo.value.email,
      oldPassword: passwordForm.value.currentPassword,
      newPassword: passwordForm.value.newPassword
    })

    // 5. 处理返回结果
    if (response.code === 200) {
      ElMessage.success('密码修改成功')
      showPasswordDialog.value = false
      // 重置表单
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
        captcha: ''
      }
      captchaImage.value = ''
      captchaKey.value = ''
    } else {
      ElMessage.error(response.message || '密码修改失败')
      // 重新获取验证码
      getCaptcha()
      passwordForm.value.captcha = ''
    }
  } catch (error) {
    // 表单验证失败
    if (error !== false) { // Element Plus表单验证失败时会返回false
      ElMessage.error('请检查输入内容')
    }
    console.warn('密码修改表单验证失败:', error)
  }
}

// 保存阅读偏好 - 功能已集成到设置中

// 格式化日期 - 功能已移除
// const formatDate = (dateStr) => {
//   if (!dateStr) return ''
//   const date = new Date(dateStr)
//   return date.toLocaleDateString('zh-CN', {
//     year: 'numeric',
//     month: 'long',
//     day: 'numeric'
//   })
// }

// 格式化时间
const formatTime = (time) => {
  const now = new Date()
  const diff = now - time
  const days = Math.floor(diff / (24 * 60 * 60 * 1000))

  if (days === 0) {
    const hours = Math.floor(diff / (60 * 60 * 1000))
    if (hours === 0) {
      const minutes = Math.floor(diff / (60 * 1000))
      return minutes === 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    return time.toLocaleDateString()
  }
}

// 跳转到书籍详情页 - 功能已移除
// const goToBook = (bookId) => {
//   router.push(`/book/${bookId}`)
// }

// 继续阅读 - 功能已移除
// const continueReading = (item) => {
//   router.push(`/book/${item.bookId}/chapter/${item.lastChapterId || 1}`)
// }

// 从阅读历史中移除 - 功能已移除
// const removeFromHistory = (id) => {
//   const index = readingHistory.value.findIndex(item => item.id === id)
//   if (index > -1) {
//     readingHistory.value.splice(index, 1)
//     ElMessage.success('已从阅读历史中移除')
//   }
// }

// 处理密码弹窗关闭事件
const handlePasswordDialogClosed = () => {
  try {
    // 安全地重置表单
    if (passwordFormRef.value && typeof passwordFormRef.value.resetFields === 'function') {
      passwordFormRef.value.resetFields()
    }

    // 安全地重置表单数据
    if (passwordForm.value) {
      passwordForm.value = {
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
        captcha: ''
      }
    }

    // 重置验证码相关数据
    captchaImage.value = ''
    captchaKey.value = ''
  } catch (error) {
    console.warn('重置密码表单时出错:', error)
  }
}

// 处理邮箱弹窗关闭事件
const handleEmailDialogClosed = () => {
  try {
    // 安全地重置表单
    if (emailFormRef.value && typeof emailFormRef.value.resetFields === 'function') {
      emailFormRef.value.resetFields()
    }

    // 安全地重置表单数据
    if (emailForm.value) {
      emailForm.value = {
        newEmail: '',
        emailCode: '',
        captcha: ''
      }
    }

    // 重置验证码相关数据
    captchaImage.value = ''
    captchaKey.value = ''
    emailCountdown.value = 0
  } catch (error) {
    console.warn('重置邮箱表单时出错:', error)
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserData()
})

// 监听密码弹窗显示状态
watch(showPasswordDialog, (newVal) => {
  if (newVal) {
    getCaptcha()
  }
})

// 监听邮箱弹窗显示状态
watch(showEmailDialog, (newVal) => {
  if (newVal) {
    getCaptcha()
  }
})


</script>

<style scoped>
.user-center-page {
  min-height: calc(100vh - 60px);
  background-color: #f8fafc;
  padding-top: 80px;
}

.user-center-container {
  display: flex;
  gap: 24px;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 20px;
  height: calc(100vh - 80px);
}

/* 左侧导航栏 */
.user-sidebar {
  width: 280px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  height: fit-content;
  position: sticky;
  top: 80px;
}

.sidebar-header {
  padding: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.user-avatar-section {
  display: flex;
  align-items: center;
  gap: 16px;
}

.avatar-container {
  flex-shrink: 0;
}

.user-info {
  flex: 1;
}

.username {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 4px;
}

.user-level {
  font-size: 14px;
  opacity: 0.9;
}

.sidebar-nav {
  padding: 16px 0;
}

.nav-section {
  margin-bottom: 8px;
}

.nav-title {
  padding: 8px 24px;
  font-size: 12px;
  color: #999;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;
  color: #666;
  font-size: 16px;
}

.nav-item:hover {
  background-color: #f8fafc;
  color: #333;
}

.nav-item.active {
  background-color: #f0f7ff;
  color: #409eff;
  font-weight: 500;
}

.nav-item.active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  background: #409eff;
  border-radius: 0 2px 2px 0;
}

.nav-item .el-icon {
  font-size: 20px;
}

.nav-divider {
  height: 1px;
  background-color: #e4e7ed;
  margin: 16px 24px;
}

.message-badge {
  margin-left: auto;
}

/* 右侧内容区 */
.user-content {
  flex: 1;
  overflow-y: auto;
}

.content-panel {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  margin-bottom: 24px;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 32px;
  border-bottom: 1px solid #f0f0f0;
  background: #fafbfc;
}

.panel-title {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.panel-actions {
  display: flex;
  gap: 12px;
}

.panel-body {
  padding: 32px;
}

/* 消息标签页头部样式 */
.message-tabs-header {
  padding: 16px 0;
  border-bottom: 1px solid #e4e7ed;
  margin-bottom: 16px;
}

.message-tabs {
  width: 100%;
}

.message-tabs .el-tabs__header {
  margin-bottom: 0;
}

.message-tabs .el-tabs__nav {
  border: none;
}

.message-tabs .el-tabs__item {
  padding: 0 20px;
  height: 40px;
  line-height: 40px;
  border: none;
  font-size: 14px;
  color: #606266;
  transition: all 0.3s ease;
}

.message-tabs .el-tabs__item:hover {
  color: #409eff;
  background-color: #f5f7fa;
}

.message-tabs .el-tabs__item.is-active {
  color: #409eff;
  background-color: transparent;
  border-bottom: 2px solid #409eff;
}

.tabs-actions {
  display: flex;
  gap: 12px;
}

.action-button {
  padding: 8px 16px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background-color: #fff;
  color: #606266;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  user-select: none;
}

.action-button:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

.action-button.disabled {
  cursor: not-allowed;
  opacity: 0.6;
  color: #c0c4cc;
  border-color: #ebeef5;
  background-color: #f5f7fa;
}

.action-button.disabled:hover {
  color: #c0c4cc;
  border-color: #ebeef5;
  background-color: #f5f7fa;
}

.action-button.danger {
  color: #f56c6c;
  border-color: #fab6b6;
}

.action-button.danger:hover {
  color: #fff;
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.message-content-area {
  padding-top: 16px;
}

.message-panel {
  min-height: 400px;
}

/* 内容区域样式 */
.profile-view {
  max-width: 600px;
}

.info-item {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 8px;
}

.info-label {
  width: 80px;
  color: #666;
  font-weight: 500;
}

.info-value {
  flex: 1;
  color: #333;
}

.profile-form {
  max-width: 600px;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

/* 书籍网格 */
.book-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.book-card {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
}

.book-card:hover {
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.book-cover {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

/* 编辑个人资料样式 */
.profile-tabs {
  margin-bottom: 20px;
}

.edit-form .el-form-item {
  margin-bottom: 24px;
}

.form-row {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
}

.account-value {
  font-size: 16px;
  color: #333;
  min-width: 200px;
}

.modify-btn {
  margin-left: auto;
  background-color: transparent;
  color: #f56c6c;
  border-color: #f56c6c;
}

.modify-btn:hover {
  background-color: rgba(245, 108, 108, 0.1);
}

.nickname-input {
  width: 300px;
}

.limit-text {
  color: #999;
  font-size: 14px;
  margin-left: 10px;
}

.account-type {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 16px;
  color: #333;
}

.vip-icon {
  color: #f7ba2a;
  font-size: 18px;
}

/* 评论列表样式 */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  background-color: #fff;
  padding: 16px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  cursor: pointer;
  transition: all 0.3s ease;
}

.review-item:hover {
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.review-content {
  color: #333;
  flex: 1;
  margin-right: 16px;
}

.review-text {
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 8px;
}

.review-book {
  font-size: 12px;
  color: #666;
  margin-bottom: 4px;
}

.review-time {
  font-size: 12px;
  color: #999;
}

.review-actions {
  display: flex;
  gap: 8px;
}

.gender-radio {
  display: flex;
  gap: 20px;
}

.phone-value {
  font-size: 16px;
  color: #333;
  min-width: 150px;
}

.signature-textarea {
  width: 100%;
  max-width: 500px;
}

.form-actions {
  display: flex;
  gap: 15px;
  margin-top: 30px;
}

.cancel-btn {
  padding: 8px 30px;
}

.save-btn {
  padding: 8px 30px;
  background-color: #f56c6c;
  border-color: #f56c6c;
}

.save-btn:hover {
  background-color: #f78989;
  border-color: #f78989;
}

/* 头像编辑样式 */
.avatar-edit {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 30px;
}

.avatar-preview {
  margin-bottom: 30px;
}

.current-avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #dcdfe6;
}

.avatar-uploader {
  margin-bottom: 15px;
}

.upload-tips {
  color: #999;
  font-size: 14px;
}

.book-info {
  padding: 16px;
}

.book-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.book-author {
  font-size: 14px;
  color: #666;
  margin-bottom: 12px;
}

.book-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}

.book-actions {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

/* 用户信息区域 */
.user-info-panel {
  margin-bottom: 24px; /* 调整为与卡片间距相同的24px */
  background: white; /* 用户信息区域背景设为纯白 */
  padding: 32px; /* 独立的内边距 */
}

.cards-panel {
  /* 卡片区域面板，独立的内边距 */
  background: white;
  padding: 32px;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

/* 信息卡片 */
.info-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  padding: 24px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.info-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
}

.card-header {
  margin-bottom: 20px;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}

.card-body {
  margin-bottom: 20px;
}

.stats-divider {
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 80px;
}

/* 评论统计三角形布局 */
.review-stats-triangle {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 120px;
  padding: 16px 0;
}

.triangle-top {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.triangle-bottom {
  display: flex;
  justify-content: space-around;
  width: 100%;
  max-width: 200px;
  position: relative;
}

.triangle-bottom::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 20%;
  bottom: 20%;
  width: 1px;
  background-color: #e4e7ed;
  transform: translateX(-50%);
}

.triangle-top .stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 4px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.triangle-top .stat-number:hover {
  color: #f56c6c;
}

.triangle-top .stat-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.triangle-bottom .stat-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 16px;
}

.triangle-bottom .stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.triangle-bottom .stat-number:hover {
  color: #f56c6c;
}

.triangle-bottom .stat-label {
  font-size: 12px;
  color: #333;
}

.stat-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  flex: 1;
}

.divider-vertical {
  width: 1px;
  height: 60px;
  background-color: #e4e7ed;
  margin: 0 20px;
}

.card-footer {
  text-align: center;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
}

.stat-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 24px;
  border-radius: 12px;
  text-align: center;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  opacity: 0.9;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #ddd;
}

.empty-text {
  font-size: 16px;
  margin-bottom: 20px;
}

/* 动态列表 */
.dynamic-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.dynamic-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border-left: 4px solid #409eff;
}

.dynamic-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
}

.dynamic-content {
  flex: 1;
}

.dynamic-text {
  margin-bottom: 8px;
  color: #333;
}

.dynamic-book {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 12px;
  padding: 12px;
  background: white;
  border-radius: 8px;
}

.dynamic-book-cover {
  width: 40px;
  height: 55px;
  object-fit: cover;
  border-radius: 4px;
}

.dynamic-book-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.dynamic-time {
  font-size: 12px;
  color: #999;
  margin-top: 8px;
}

/* 评论列表 */
.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  border-left: 4px solid #67c23a;
}

.review-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.review-text {
  color: #333;
  line-height: 1.6;
  font-size: 14px;
}

.review-book {
  font-size: 13px;
  color: #409eff;
  font-weight: 500;
}

.review-time {
  font-size: 12px;
  color: #999;
}

/* 消息列表 */
.message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.message-item.unread {
  background: #fff;
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.message-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.message-icon {
  font-size: 20px;
  color: #409eff;
  margin-top: 2px;
}

.message-content {
  flex: 1;
}

.message-title.username {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.message-text {
  color: #666;
  margin-bottom: 8px;
  line-height: 1.5;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.message-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 设置项 */
.settings-sections {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.settings-section {
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.settings-section h3 {
  padding: 20px 24px;
  margin: 0;
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.setting-item:last-child {
  border-bottom: none;
}

.setting-info {
  flex: 1;
}

.setting-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.setting-desc {
  font-size: 12px;
  color: #999;
}

.setting-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.profile-form {
  max-width: 600px;
}

.book-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.book-item {
  display: flex;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.book-item:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.book-cover {
  position: relative;
  width: 80px;
  height: 112px;
  margin-right: 15px;
  overflow: hidden;
  border-radius: 4px;
}

.book-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.reading-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.7);
  color: #fff;
  font-size: 12px;
  text-align: center;
  padding: 2px 0;
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 16px;
  margin-bottom: 5px;
  color: #333;
}

.book-author, .book-category, .book-status, .last-read, .last-read-time {
  font-size: 12px;
  color: #666;
  margin-bottom: 3px;
}

.book-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

.history-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.history-item {
  display: flex;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.history-item:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.history-info {
  flex: 1;
}

.last-chapter, .read-time {
  font-size: 12px;
  color: #666;
  margin-bottom: 3px;
}

.history-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  justify-content: center;
}

.recommendation-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.recommendation-item {
  display: flex;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
}

.recommendation-info {
  flex: 1;
  margin-left: 15px;
}

.recommendation-time, .recommendation-status {
  font-size: 12px;
  color: #666;
  margin-bottom: 3px;
}

.message-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-item {
  display: flex;
  padding: 15px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.message-item:hover {
  background-color: #f9f9f9;
}

.message-item.unread {
  background-color: #f0f9ff;
  border-color: #b3d8ff;
}

.message-icon {
  margin-right: 15px;
  font-size: 20px;
  color: #666;
}

.message-content {
  flex: 1;
}

.message-title {
  font-size: 16px;
  margin-bottom: 5px;
  color: #333;
}

.message-text {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.message-time {
  font-size: 12px;
  color: #999;
}

.settings-sections {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.settings-section {
  max-width: 600px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.settings-section:last-child {
  border-bottom: none;
}

.settings-section h3 {
  font-size: 16px;
  margin-bottom: 15px;
  color: #333;
}

.message-badge {
  margin-left: auto;
}

/* 个人中心样式 */
.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
}

.profile-avatar img {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 20px;
}

.profile-basic-info {
  flex: 1;
}

.profile-basic-info .username {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #333;
}

.likes-count {
  color: #f56c6c;
  font-size: 14px;
  font-weight: 500;
}

.user-signature {
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 24px;
  padding: 12px 0;
  border-left: 3px solid #409eff;
  padding-left: 16px;
  background-color: #f8fafc;
  border-radius: 0 6px 6px 0;
}

.profile-section {
  margin-bottom: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: #333;
}

.stats-grid, .info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-item, .info-item {
  text-align: center;
  padding: 20px;
  background: #f9f9f9;
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-item:hover, .info-item:hover {
  background: #f0f0f0;
  transform: translateY(-2px);
}.stat-number, .info-number.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #333;
  margin-bottom: 8px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.3s ease;
}

.interaction-stats .stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
  cursor: pointer;
  transition: color 0.3s ease;
}

.interaction-stats .stat-number:hover {
  color: #f56c6c;
}

.interaction-stats .stat-label {
  font-size: 12px;
  color: #333;
}

.stat-label, .info-label {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

/* 用户互动卡片新布局样式 */
.interaction-stats {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 120px;
  padding: 16px 0;
}

.top-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.triangle-bottom {
  display: flex;
  justify-content: center;
  width: 100%;
  max-width: 200px;
  position: relative;
  align-items: center;
}

.triangle-bottom::before {
  content: '';
  position: absolute;
  left: 50%;
  top: 20%;
  bottom: 20%;
  width: 1px;
  background-color: #e4e7ed;
  transform: translateX(-50%);
}

.triangle-bottom .stat-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 16px;
  justify-content: center;
  flex: 1;
}

/* 未读消息徽章样式 */
.tabs-actions {
  display: flex;
  gap: 15px;
  margin-top: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
  justify-content: space-between;
  align-items: center;
}

.message-stats {
  display: flex;
  align-items: center;
}

.total-count {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.action-button {
  padding: 8px 16px;
  background-color: #409eff;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.3s;
}

.action-button:hover {
  background-color: #66b1ff;
}

.action-button.disabled {
  background-color: #c0c4cc;
  cursor: not-allowed;
}

.action-button.disabled:hover {
  background-color: #c0c4cc;
}

.action-button.danger {
  background-color: #f56c6c;
}

.action-button.danger:hover {
  background-color: #f78989;
}
.unread-badge {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%);
  background-color: #f56c6c;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
  font-weight: 600;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .user-center-container {
    max-width: 1140px;
  }
}

@media (max-width: 992px) {
  .user-center-container {
    max-width: 960px;
  }

  .user-sidebar {
    width: 260px;
  }

  .panel-header {
    padding: 20px 24px;
  }

  .panel-body {
    padding: 24px;
  }
}

@media (max-width: 768px) {
  .user-center-page {
    padding-top: 0;
  }

  .user-center-container {
    flex-direction: column;
    height: auto;
    gap: 16px;
    padding: 16px;
  }

  .user-sidebar {
    width: 100%;
    position: static;
  }

  .sidebar-header {
    padding: 20px;
  }

  .user-content {
    overflow: visible;
  }

  .content-panel {
    margin-bottom: 16px;
  }

  .panel-header {
    padding: 16px 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .panel-body {
    padding: 20px;
  }

  .nav-section {
    display: flex;
    flex-wrap: wrap;
    gap: 8px;
  }

  .nav-title {
    width: 100%;
    padding: 8px 20px;
  }

  .nav-item {
    flex: 1;
    min-width: 120px;
    justify-content: center;
    padding: 10px 16px;
    border-radius: 8px;
  }
}

@media (max-width: 576px) {
  .user-center-container {
    padding: 12px;
    gap: 12px;
  }

  .sidebar-header {
    padding: 16px;
  }

  .user-avatar-section {
    gap: 12px;
  }

  .username {
    font-size: 16px;
  }

  .user-level {
    font-size: 13px;
  }

  .nav-section {
    gap: 6px;
  }

  .nav-title {
    padding: 6px 16px;
    font-size: 11px;
  }

  .nav-item {
    min-width: 100px;
    padding: 8px 12px;
    font-size: 13px;
    gap: 8px;
  }

  .panel-header {
    padding: 14px 16px;
  }

  .panel-title {
    font-size: 18px;
  }

  .panel-body {
    padding: 16px;
  }

  .panel-actions {
    gap: 8px;
  }
}
</style>
