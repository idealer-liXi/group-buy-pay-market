import { createRouter, createWebHistory } from 'vue-router'
import { getCookie } from '../lib/cookie'

const LoginPage = () => import('../pages/LoginPage.vue')
const GoodsPage = () => import('../pages/GoodsPage.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/goods/9890001' },
    { path: '/login', component: LoginPage },
    { path: '/goods/:goodsId', component: GoodsPage }
  ]
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !getCookie('loginToken')) {
    return '/login'
  }
})

export default router
