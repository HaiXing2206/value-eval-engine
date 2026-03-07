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
import Register from '../views/Register.vue'
import { canAccessPath, getDefaultPathByRole } from '../auth/roles'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login },
    { path: '/register', component: Register },
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
  const role = localStorage.getItem('vee_role')

  if (to.path === '/login' || to.path === '/register') {
    if (token) {
      return getDefaultPathByRole(role)
    }
    return true
  }

  if (!token) {
    return '/login'
  }

  if (!canAccessPath(role, to.path)) {
    return getDefaultPathByRole(role)
  }

  return true
})

export default router
