import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";

// 全局使用element ui 2.x
import Element from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
Vue.use(Element);

// 全局使用axios
import axios from "axios";
Vue.prototype.$axios = axios;
//请求头前缀
axios.defaults.baseURL = "http://localhost:8088";
//axios请求携带cookie
axios.defaults.withCredentials = true;

Vue.config.productionTip = false;

new Vue({
  router,
  store,
  render: (h) => h(App),
}).$mount("#app");
