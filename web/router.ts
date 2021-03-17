import Vue from "vue";
import Router from "vue-router";

import Authentication from "./components/Authentication.vue";
import Email from "./components/Email.vue";


Vue.use(Router);

const router = new Router({
    linkActiveClass: "active",
    base: "/inbox",
    routes: [
        { path: "/", component: Authentication, name: "authentication" },
        { path: "/:folder", name: "folder" },
        { path: "/:folder/email/:id", component: Email, name: "email" },
    ]
});

export default router;
