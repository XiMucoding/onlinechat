import Vue from "vue";
import VueRouter from "vue-router";
import Websocket from "../views/websocket.vue";
import Netty from "../views/netty.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/websocket",
    name: "Websocket",
    component: Websocket,
  },
  {
    path: "/netty",
    name: "Netty",
    component: Netty,
  },
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
