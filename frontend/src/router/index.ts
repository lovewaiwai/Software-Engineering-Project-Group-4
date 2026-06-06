import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'

import AppLayout from '../layouts/AppLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'

import LoginView from '../views/auth/LoginView.vue'
import RegisterView from '../views/auth/RegisterView.vue'
import VerifyView from '../views/auth/VerifyView.vue'

import HomeView from '../views/home/HomeView.vue'
import ProductListView from '../views/product/ProductListView.vue'
import ProductDetailView from '../views/product/ProductDetailView.vue'
import ProductCreateView from '../views/product/ProductCreateView.vue'
import MyProductsView from '../views/product/MyProductsView.vue'
import OrderListView from '../views/order/OrderListView.vue'
import OrderDetailView from '../views/order/OrderDetailView.vue'
import ChatView from '../views/chat/ChatView.vue'
import ProfileView from '../views/profile/ProfileView.vue'
import PointsView from '../views/points/PointsView.vue'

import AdminDashboardView from '../views/admin/AdminDashboardView.vue'
import AdminProductsView from '../views/admin/AdminProductsView.vue'
import AdminReportsView from '../views/admin/AdminReportsView.vue'
import AdminUsersView from '../views/admin/AdminUsersView.vue'
import AdminLockersView from '../views/admin/AdminLockersView.vue'

import { useAuthStore } from '../stores/auth'
import { fetchCurrentUser } from '../api/user'


const routes: RouteRecordRaw[] = [
  { path: '/login', component: LoginView, meta: { guest: true } },
  { path: '/register', component: RegisterView, meta: { guest: true } },
  { path: '/verify', component: VerifyView, meta: { requiresAuth: true, requiresUser: true } },
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', component: HomeView },
      { path: 'products', component: ProductListView },
      { path: 'products/new', component: ProductCreateView, meta: { requiresAuth: true, requiresUser: true, requiresVerified: true } },
      { path: 'products/mine', component: MyProductsView, meta: { requiresAuth: true, requiresUser: true } },
      { path: 'products/:id', component: ProductDetailView, props: true },
      { path: 'orders', component: OrderListView, meta: { requiresAuth: true, requiresUser: true, requiresVerified: true } },
      { path: 'orders/:id', component: OrderDetailView, props: true, meta: { requiresAuth: true, requiresUser: true, requiresVerified: true } },
      { path: 'chat', component: ChatView, meta: { requiresAuth: true, requiresUser: true, requiresVerified: true } },
      { path: 'profile/:id', component: ProfileView, props: true, meta: { requiresAuth: true } },
      { path: 'points', component: PointsView, meta: { requiresAuth: true, requiresUser: true } },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdmin: true },
    children: [
      { path: '', component: AdminDashboardView, meta: { requiresSystemReviewer: true } },
      { path: 'products', component: AdminProductsView, meta: { requiresProductReviewer: true } },
      { path: 'reports', component: AdminReportsView, meta: { requiresSystemReviewer: true } },
      { path: 'users', component: AdminUsersView, meta: { requiresSystemReviewer: true } },
      { path: 'lockers', component: AdminLockersView, meta: { requiresSystemReviewer: true } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

function defaultHomePath(auth: ReturnType<typeof useAuthStore>) {
  if (auth.role === 'PRODUCT_REVIEWER') return '/admin/products'
  return auth.isAdmin ? '/admin' : '/'
}

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (auth.isAdmin && !to.path.startsWith('/admin') && !to.meta.guest) {
    return { path: defaultHomePath(auth) }
  }
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && (!auth.isLoggedIn || !auth.isAdmin)) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresSystemReviewer && !auth.isSystemReviewer) {
    return auth.canReviewProducts ? { path: '/admin/products' } : { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresProductReviewer && !auth.canReviewProducts) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresUser && auth.isAdmin) {
    return { path: defaultHomePath(auth) }
  }
  if (to.meta.requiresVerified && auth.isLoggedIn && !auth.isVerified) {
    try {
      const response = await fetchCurrentUser()
      if (response.code === 0) {
        auth.updateProfile(response.data.profile)
      }
    } catch {
      // keep local state when refresh fails
    }
  }
  if (to.meta.requiresVerified && auth.isLoggedIn && !auth.isVerified) {
    return { path: '/verify', query: { redirect: to.fullPath } }
  }
  if (to.meta.guest && auth.isLoggedIn) {
    return defaultHomePath(auth)
  }
  return true
})

export default router
