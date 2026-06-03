import { defineStore } from 'pinia'
import { cookieUtil } from '../utils/request'
import { userApi } from '../api/Users'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isAuthenticated: false,
    isAdmin: false,
    loading: false,
  }),

  getters: {
    isLoggedIn: (state) => state.isAuthenticated,
    userName: (state) => state.user?.username || '',
    userId: (state) => state.user?.id || null,
  },

  actions: {
    // 登录
    async login(credentials) {
      this.loading = true
      try {
        // 调用真实的登录API
        const response = await userApi.login(credentials)

        // console.log('登录响应:', response)

        // 从后端响应中获取真实的token和用户信息
        // 根据返回数据结构，user在response.data中，token直接在response中
        const user = response.data
        const token = response.token
        const { accessToken, refreshToken } = token

        // 更新状态
        // console.log('用户角色:', user.roleName)
        this.user = user
        this.token = accessToken
        this.isAuthenticated = true
        this.isAdmin = user.roleName === 'ROLE_ADMIN'
        // console.log(this.isAdmin)
        // console.log('用户信息:', this.user)
        // 保存到cookie
        cookieUtil.set('token', accessToken, { expires: 7, path: '/', sameSite: 'Lax' })
        cookieUtil.set('refreshToken', refreshToken, { expires: 14, path: '/', sameSite: 'Lax' })
        cookieUtil.set('user', JSON.stringify(user), { expires: 7, path: '/', sameSite: 'Lax' })
        // console.log('用户信息:', JSON.parse(cookieUtil.get('user')))
        return { success: true, user }
      } catch (error) {
        console.error('登录失败:', error)
        return { success: false, message: '登录失败，请检查用户名和密码' }
      } finally {
        this.loading = false
      }
    },

    // 注册
    async register(userData) {
      this.loading = true
      try {
        // 调用真实的注册API
        const response = await userApi.register(userData)

        console.log('注册响应:', response)

        // 从后端响应中获取真实的用户信息
        const user = response.data

        return { success: true, user }
      } catch (error) {
        console.error('注册失败:', error)
        return { success: false, message: '注册失败，请稍后再试' }
      } finally {
        this.loading = false
      }
    },

    // 登出
    logout() {
      this.user = null
      this.token = null
      this.isAuthenticated = false
      this.isAdmin = false

      // 清除cookie
      cookieUtil.remove('token', { path: '/' })
      cookieUtil.remove('user', { path: '/' })

      // 清除本地存储作为备份
      localStorage.removeItem('user')
      localStorage.removeItem('token')
    },

    // 初始化认证状态（从cookie恢复）
    async initAuth() {
      // 从cookie获取数据
      let storedToken = cookieUtil.get('token')
      // console.log('storedToken:', storedToken)

      // 确保storedToken是字符串类型
      storedToken = typeof storedToken === 'string' ? storedToken : null

      if (storedToken) {
        try {
          // 如果有token，直接调用API获取最新用户信息
          this.token = storedToken
          const response = await userApi.getUserInfo()
          if (response.code === 200) {
            this.user = response.data
            this.isAuthenticated = true
            this.isAdmin = this.user.roleName === 'ROLE_ADMIN'
            // 更新cookie中的用户信息
            cookieUtil.set('user', JSON.stringify(this.user), {
              expires: 7,
              path: '/',
              sameSite: 'Lax',
            })
          } else {
            throw new Error('获取用户信息失败')
          }
        } catch (error) {
          console.error('初始化认证状态失败:', error)
          this.logout()
        }
      } else {
        // 兼容处理：从localStorage获取数据
        const localStorageToken = localStorage.getItem('token')
        // console.log('localStorageToken:', localStorageToken)

        if (localStorageToken) {
          try {
            // 如果localStorage中有token，调用API获取最新用户信息
            this.token = localStorageToken
            const response = await userApi.getUserInfo()
            if (response.code === 200) {
              this.user = response.data
              this.isAuthenticated = true
              this.isAdmin = this.user.roleName === 'ROLE_ADMIN'
              // 将数据迁移到cookie
              cookieUtil.set('user', JSON.stringify(this.user), {
                expires: 7,
                path: '/',
                sameSite: 'Lax',
              })
              cookieUtil.set('token', localStorageToken, { expires: 7, path: '/', sameSite: 'Lax' })
            } else {
              throw new Error('获取用户信息失败')
            }
          } catch (error) {
            console.error('初始化认证状态失败:', error)
            this.logout()
          }
        }
      }
    },

    // 更新用户信息
    async updateProfile(profileData) {
      this.loading = true
      try {
        // 模拟API请求
        await new Promise((resolve) => setTimeout(resolve, 1000))

        // 更新用户信息
        this.user = { ...this.user, ...profileData }
        // 更新管理员状态
        this.isAdmin = this.user.roleName === 'ROLE_ADMIN'
        // 更新cookie
        cookieUtil.set('user', JSON.stringify(this.user), {
          expires: 7,
          path: '/',
          sameSite: 'Lax',
        })
        // 更新localStorage作为备份
        localStorage.setItem('user', JSON.stringify(this.user))

        return { success: true, user: this.user }
      } catch (error) {
        console.error('更新用户信息失败:', error)
        return { success: false, message: '更新失败，请稍后再试' }
      } finally {
        this.loading = false
      }
    },

    // 修改密码
    // async changePassword() {
    //   this.loading = true
    //   try {
    //     return { success: true, message: '密码修改成功' }
    //   } catch (error) {
    //     console.error('修改密码失败:', error)
    //     return { success: false, message: '修改密码失败，请稍后再试' }
    //   } finally {
    //     this.loading = false
    //   }
    // },
  },
})
