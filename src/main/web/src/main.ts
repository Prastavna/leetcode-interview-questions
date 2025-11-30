import { createApp } from "vue";
import "./style.css";
import ui from "@nuxt/ui/vue-plugin";
import { createRouter, createWebHistory } from "vue-router";
import App from "./App.vue";
// Initialize theme early to prevent flash
import "./utils/useTheme";

const router = createRouter({
	routes: [],
	history: createWebHistory(),
});

createApp(App).use(router).use(ui).mount("#app");
