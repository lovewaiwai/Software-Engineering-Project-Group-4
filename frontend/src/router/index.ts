import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
import AdminLayout from '../layouts/AdminLayout.vue'
import LoginView from '../views/auth/LoginView.vue'
import VerifyView from '../views/auth/VerifyView.vue'
import HomeView from '../views/home/HomeView.vue'
import ProductListView from '../views/product/ProductListView.vue'
import ProductDetailView from '../views/product/ProductDetailView.vue'
import ProductCreateView from '../views/product/ProductCreateView.vue'
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

const routes: RouteRecordRaw[] = [
  { path: '/login', component: LoginView, meta: { guest: true } },
  { path: '/verify', component: VerifyView },
  {
    path: '/',
    component: AppLayout,
    children: [
      { path: '', component: HomeView },
      { path: 'products', component: ProductListView },
      { path: 'products/new', component: ProductCreateView },
      { path: 'products/:id', component: ProductDetailView, props: true },
      { path: 'orders', component: OrderListView },
      { path: 'orders/:id', component: OrderDetailView, props: true },
      { path: 'chat', component: ChatView, meta: { requiresAuth: true, requiresUser: true } },
      { path: 'profile/:id', component: ProfileView, props: true },
      { path: 'points', component: PointsView },
    ],
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAdmin: true },
    children: [
      { path: '', component: AdminDashboardView },
      { path: 'products', component: AdminProductsView },
      { path: 'reports', component: AdminReportsView },
      { path: 'users', component: AdminUsersView },
      { path: 'lockers', component: AdminLockersView },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

function defaultHomePath(auth: ReturnType<typeof useAuthStore>) {
  return auth.isAdmin ? '/admin' : '/'
}

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { path: '/login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresUser && auth.isAdmin) {
    return { path: '/admin' }
  }
  if (to.meta.guest && auth.isLoggedIn) {
    return defaultHomePath(auth)
  }
  return true
})

export default router
