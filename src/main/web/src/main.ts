import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ui from '@nuxt/ui/vue-plugin'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  routes: [],
  history: createWebHistory()
})

createApp(App).use(router).use(ui).mount('#app')
