import { createRouter, createWebHistory } from 'vue-router';
import Dashboard from '../views/dashboard/Dashboard.vue';
import Staff from '../views/staff/Staff.vue';
import Service from '../views/service/Service.vue';
import Order from '../views/order/Order.vue';

const routes = [
  { path: '/', redirect: '/dashboard' },
  { path: '/dashboard', component: Dashboard },
  { path: '/staff', component: Staff },
  { path: '/service', component: Service },
  { path: '/order', component: Order }
];
export default createRouter({ history: createWebHistory(), routes });
