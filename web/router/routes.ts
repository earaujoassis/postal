import Email from "../views/email.vue";
import Settings from "../views/settings.vue";
import EmptyMessage from "../components/emptyMessage.vue";
import EmailMessage from "../components/emailMessage.vue";

const routes = [
    { path: "/settings", component: Settings },
    { path: "/:folder", component: Email,
        children: [
            { path: "", component: EmptyMessage },
            { path: "/email/:id", component: EmailMessage },
        ]
    }
];

export default routes;
