import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'
import Dashboard from '../views/Dashboard.vue'
import Indicators from '../views/Indicators.vue'
import Rules from '../views/Rules.vue'
import Weights from '../views/Weights.vue'
import Calibers from '../views/Calibers.vue'
import Tasks from '../views/Tasks.vue'
import TaskHistory from '../views/TaskHistory.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
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
