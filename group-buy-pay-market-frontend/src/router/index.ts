import { createRouter, createWebHistory } from 'vue-router'
import { getAdminToken } from '../lib/admin-auth'
import { getCookie } from '../lib/cookie'

const LoginPage = () => import('../pages/LoginPage.vue')
const GoodsListPage = () => import('../pages/GoodsListPage.vue')
const GoodsPage = () => import('../pages/GoodsPage.vue')
const MockPayPage = () => import('../pages/MockPayPage.vue')
const PurchaseHistoryPage = () => import('../pages/PurchaseHistoryPage.vue')
const AdminLoginPage = () => import('../pages/admin/AdminLoginPage.vue')
const AdminGoodsPage = () => import('../pages/admin/AdminGoodsPage.vue')
const AdminDiscountsPage = () => import('../pages/admin/AdminDiscountsPage.vue')
const AdminActivitiesPage = () => import('../pages/admin/AdminActivitiesPage.vue')
const AdminUsersPage = () => import('../pages/admin/AdminUsersPage.vue')
const AdminTagsPage = () => import('../pages/admin/AdminTagsPage.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/goods' },
    { path: '/login', component: LoginPage },
    { path: '/goods', component: GoodsListPage },
    { path: '/goods/:goodsId', component: GoodsPage },
    { path: '/mock-pay/:orderId', component: MockPayPage },
    { path: '/orders', component: PurchaseHistoryPage },
    { path: '/admin/login', component: AdminLoginPage },
    { path: '/admin', redirect: '/admin/goods' },
    { path: '/admin/goods', component: AdminGoodsPage },
    { path: '/admin/discounts', component: AdminDiscountsPage },
    { path: '/admin/activities', component: AdminActivitiesPage },
    { path: '/admin/users', component: AdminUsersPage },
    { path: '/admin/tags', component: AdminTagsPage }
  ]
})

router.beforeEach((to) => {
  if (to.path.startsWith('/admin')) {
    if (to.path !== '/admin/login' && !getAdminToken()) {
      return '/admin/login'
    }
    return
  }

  if (to.path !== '/login' && !getCookie('loginToken')) {
    return '/login'
  }
})

export default router
