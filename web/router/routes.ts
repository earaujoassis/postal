import Email from "@/views/Email.vue";
import Settings from "@/views/Settings.vue";
import EmptyMessage from "@/components/EmptyMessage.vue";
import EmailMessage from "@/components/EmailMessage.vue";

const routes = [
    { path: "/", redirect: "/inbox" },
    { path: "/settings", component: Settings },
    { path: "/:folder", component: Email,
        children: [
            { path: "", component: EmptyMessage },
            { path: "email/:id", component: EmailMessage },
        ]
    }
];

export default routes;
