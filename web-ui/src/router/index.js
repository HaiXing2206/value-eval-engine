import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import Indicators from '../views/Indicators.vue'
import Rules from '../views/Rules.vue'
import Weights from '../views/Weights.vue'
import Calibers from '../views/Calibers.vue'
import Tasks from '../views/Tasks.vue'
import TaskHistory from '../views/TaskHistory.vue'
import Login from '../views/Login.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        { path: '/dashboard', component: Dashboard },
        { path: '/indicators', component: Indicators },
        { path: '/rules', component: Rules },
        { path: '/weights', component: Weights },
        { path: '/calibers', component: Calibers },
        { path: '/tasks', component: Tasks },
        { path: '/history', component: TaskHistory }
      ]
    }
  ]
})


router.beforeEach((to) => {
  const token = localStorage.getItem('vee_token')
  if (to.path === '/login') {
    if (token) {
      return '/dashboard'
    }
    return true
  }

  if (!token) {
    return '/login'
  }

  return true
})

export default router
