import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'
import Indicators from '../views/Indicators.vue'
import Rules from '../views/Rules.vue'
import Weights from '../views/Weights.vue'
import Calibers from '../views/Calibers.vue'
import Tasks from '../views/Tasks.vue'

export default createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: Layout,
      redirect: '/indicators',
      children: [
        { path: '/indicators', component: Indicators },
        { path: '/rules', component: Rules },
        { path: '/weights', component: Weights },
        { path: '/calibers', component: Calibers },
        { path: '/tasks', component: Tasks }
      ]
    }
  ]
})
