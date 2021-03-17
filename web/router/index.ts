import Vue from "vue";
import Router from "vue-router";

import checkUserAuth from "../middlewares/authentication";
import routes from "./routes";

Vue.use(Router);

const router = new Router({
    linkActiveClass: "active",
    base: "/",
    routes: routes
});

router.beforeEach((_to, _from, next: Function) => {
    checkUserAuth(next, true)
});

export default router;
