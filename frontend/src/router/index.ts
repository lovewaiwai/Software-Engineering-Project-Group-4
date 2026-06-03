import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import AppLayout from '../layouts/AppLayout.vue'
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

const routes: RouteRecordRaw[] = [
  { path: '/login', component: LoginView },
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
      { path: 'chat', component: ChatView },
      { path: 'profile/:id', component: ProfileView, props: true },
      { path: 'points', component: PointsView },
      { path: 'admin', component: AdminDashboardView },
      { path: 'admin/products', component: AdminProductsView },
      { path: 'admin/reports', component: AdminReportsView },
      { path: 'admin/users', component: AdminUsersView },
      { path: 'admin/lockers', component: AdminLockersView },
    ],
  },
]

export default createRouter({
  history: createWebHistory(),
  routes,
})
