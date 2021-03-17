<template>
    <div class="email-view">
        <div class="listing">
            <div class="listing-header">
                <p>1&ndash;10 of {{ state.total }} messages</p>
            </div>
            <ul class="entries" role="tablist">
                <li v-for="entry in state.emails" v-bind:key="entry.publicId">
                    <div
                        @click="openEmailView(entry.publicId)"
                        v-bind:id="entry.publicId"
                        v-bind:class="{ 'entry-box': true, read: entry.isRead(), active: entry.isActive(state.currentEmail) }"
                        aria-selected="false"
                        aria-controls="body-corpus-id"
                        role="tab"
                    >
                        <div class="entry-header">
                            <p class="entry-author">{{ entry.origin() }}</p>
                            <span class="entry-datetime">{{ entry.relativeTime() }}</span>
                        </div>
                        <h2 class="entry-subject">{{ entry.subject }}</h2>
                        <p class="entry-excerpt">{{ entry.excerpt() }}</p>
                    </div>
                </li>
            </ul>
        </div>
        <div class="body" id="body-corpus-id">
            <router-view v-bind:key="$route.fullPath"></router-view>
            <!-- <div v-if="state.currentEmail" class="body-header">
                <span class="user-avatar"></span>
                <div class="body-meta-box">
                    <h2 class="body-subject">{{ state.currentEmail.subject }}</h2>
                    <p><span class="user-name">{{ state.currentEmail.origin() }}</span> to {{ state.currentEmail.to[0] }}</p>
                </div>
            </div>
            <div v-if="state.currentEmail" class="body-actions">
                <ul class="actions">
                    <li>
                        <button
                            @click="markAsUnread()"
                            class="buttons button-envelope"
                        >
                            Mark as unread
                        </button>
                    </li>
                    <li>
                        <button class="buttons button-trash">Delete</button>
                    </li>
                </ul>
            </div>
            <div v-if="state.currentEmail" class="body-corpus">
                <iframe
                    @load="fixIFrameHeight"
                    ref="currentEmailIFrame"
                    v-if="state.currentEmail.bodyHTML"
                    v-bind:srcdoc="state.currentEmail.bodyHTML"
                    frameborder="0"
                    height="0"
                    scrolling="no"></iframe>
                <div v-else v-html="state.currentEmail.corpus()"></div>
            </div>
            <ul class="replies">
                <li>
                    <div class="body-reply">
                        <div class="body-header">
                            <span class="user-avatar"></span>
                            <div class="body-meta-box">
                                <p><span class="user-name">Carlos Assis</span> to wired@newsletters.wired.com</p>
                            </div>
                        </div>
                        <div class="body-corpus">
                            <p>Hey, WIRED!</p>
                            <p>
                                Integer elementum mattis massa quis placerat. Nunc non nisi pellentesque, auctor elit ut, dignissim enim.
                                Nulla interdum orci nunc, in vulputate lacus sollicitudin non. Etiam molestie condimentum nisi ut auctor. Fusce
                                sodales malesuada odio sed finibus. Curabitur accumsan neque suscipit, pellentesque dui non, porta quam.
                            </p>
                            <p>Sincerely,<br>Carlos Assis</p>
                        </div>
                    </div>
                </li>
            </ul> -->
        </div>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue, Watch } from "vue-property-decorator";
    import { State, Action } from "vuex-class";

    import { EmailsState } from "../store/modules/emails/types";

    const namespace: string = "emails";

    @Component
    export default class Email extends Vue {
        @State("emails") state!: EmailsState;
        @Action("fetchStatusData", { namespace }) fetchStatusData: any;
        @Action("fetchEmailData", { namespace }) fetchEmailData: any;
        @Action("markEmailsAsUnread", { namespace }) markEmailsAsUnread: any;
        @Action("markEmailsAsRead", { namespace }) markEmailsAsRead: any;

        mounted() {
            this.fetchEmailData(this.$route.params.id);
        }

        @Watch("$route")
        onRouterChange(to: any, _from: any) {
            this.fetchEmailData(to.params.id);
        }

        @Watch("state", { deep: true })
        markAsRead() {
            if (this.state.currentEmail) {
                let { metadata }: any = this.state.currentEmail;
                if (metadata.read === false) {
                    this.markEmailsAsRead(this.$route.params.id);
                    this.fetchStatusData();
                }
            }
        }

        markAsUnread() {
            this.markEmailsAsUnread(this.$route.params.id);
            this.fetchStatusData();
        }

        fixIFrameHeight() {
            let ref: any = this.$refs["currentEmailIFrame"];
            ref.style.height = `${ref.contentWindow.document.body.scrollHeight}` + "px";
        }
    }
</script>
