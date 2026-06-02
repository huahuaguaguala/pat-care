import { createRouter, createWebHistory } from 'vue-router';
import Layout from '../components/Layout.vue';
const routes = [
  { path: '/login', component: () => import('../views/Login.vue'), meta: { noAuth: true } },
  { path: '/', component: Layout, redirect: '/dashboard', children: [
    { path: 'dashboard', component: () => import('../views/dashboard/Dashboard.vue') },
    { path: 'staff', component: () => import('../views/staff/Staff.vue') },
    { path: 'service', component: () => import('../views/service/Service.vue') },
    { path: 'order', component: () => import('../views/order/Order.vue') },
    { path: 'billing', component: () => import('../views/billing/Billing.vue') }
  ]}
];
const router = createRouter({ history: createWebHistory(), routes });
router.beforeEach((to, from, next) => {
  if (to.meta.noAuth) return next();
  if (!localStorage.getItem('token')) return next('/login');
  next();
});
export default router;
