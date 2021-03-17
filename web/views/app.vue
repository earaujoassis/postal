<template>
    <div role="main" class="root">
        <div class="sidebar">
            <div>
                <h1 class="branding">
                <img src="//cdn.quatrolabs.com/quatrolabs-logo-white@2x.png" width="40" alt="qL" />
                <span>Postal</span>
            </h1>
            <ul class="menu">
                <li><router-link class="inbox" to="/inbox">Inbox <span class="counter">{{ state.unread }}</span></router-link></li>
                <li><router-link class="all-mail" to="/all-mail">All mail</router-link></li>
                <li><router-link class="sent" to="/sent">Sent</router-link></li>
                <li><router-link class="drafts" to="/drafts">Drafts</router-link></li>
                <li><router-link class="trash" to="/trash">Trash</router-link></li>
                <li><router-link to="/settings">Settings</router-link></li>
                <li><a href="/signout">Sign-out</a></li>
            </ul>
        </div>
        <footer class="footer">
            <p><a href="//quatrolabs.com" target="_blank">quatroLABS</a> &copy; 2016-present</p>
        </footer>
        </div>
        <router-view v-bind:key="$route.fullPath"></router-view>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue } from "vue-property-decorator";
    import { State, Action } from "vuex-class";

    import { EmailsState } from "../store/modules/emails/types";

    const namespace: string = "emails";

    @Component
    export default class App extends Vue {
        @State("emails") state!: EmailsState;
        @Action("fetchEmailsData", { namespace }) fetchEmailsData: any;
        @Action("fetchStatusData", { namespace }) fetchStatusData: any;

        mounted() {
            this.fetchStatusData();
            this.fetchEmailsData();
        }

        openEmailView(publicId: string) {
            this.$router.push(`/${this.state.folder}/email/${publicId}`);
        }
    }
</script>
