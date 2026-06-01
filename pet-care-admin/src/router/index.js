import { createRouter, createWebHistory } from 'vue-router';
const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: () => import('../views/dashboard/Dashboard.vue') },
  { path: '/staff', component: () => import('../views/staff/Staff.vue') },
  { path: '/service', component: () => import('../views/service/Service.vue') },
  { path: '/order', component: () => import('../views/order/Order.vue') }
];
export default createRouter({ history: createWebHistory(), routes });
