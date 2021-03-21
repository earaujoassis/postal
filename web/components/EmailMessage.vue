<template>
    <div>
        <div v-if="state.currentEmail" class="body-header">
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
                    <button
                        @click="moveToTrash()"
                        class="buttons button-trash"
                    >
                        Delete
                    </button>
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
        <!-- <ul class="replies">
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
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue, Watch } from "vue-property-decorator";
    import { State, Action } from "vuex-class";

    import { EmailsState } from "@/store/modules/emails/types";

    const namespace: string = "emails";

    @Component
    export default class EmailMessage extends Vue {
        @State("emails") state!: EmailsState;
        @Action("fetchStatus", { namespace }) fetchStatus: any;
        @Action("fetchEmail", { namespace }) fetchEmail: any;
        @Action("fetchEmails", { namespace }) fetchEmails: any;
        @Action("markEmailsAsUnread", { namespace }) markEmailsAsUnread: any;
        @Action("markEmailsAsRead", { namespace }) markEmailsAsRead: any;
        @Action("moveEmailToFolder", { namespace }) moveEmailToFolder: any;

        mounted() {
            this.fetchEmail(this.$route.params.id);
        }

        @Watch("$route")
        onRouterChange(to: any, _from: any) {
            this.fetchEmail(to.params.id);
        }

        @Watch("state", { deep: true })
        markAsRead() {
            if (this.state.currentEmail) {
                let { metadata }: any = this.state.currentEmail;
                if (metadata.read === false) {
                    this.markEmailsAsRead(this.$route.params.id);
                    this.fetchStatus();
                }
            }
        }

        markAsUnread() {
            this.markEmailsAsUnread(this.$route.params.id);
            this.fetchStatus();
        }

        moveToTrash() {
            this.moveEmailToFolder({ publicId: this.$route.params.id, folder: 'trash' });
            this.fetchEmails(this.state.folder);
            this.fetchStatus();
        }

        fixIFrameHeight() {
            let ref: any = this.$refs["currentEmailIFrame"];
            ref.style.height = `${ref.contentWindow.document.body.scrollHeight}` + "px";
        }
    }
</script>

<style lang="less" scope>
@user-avatar-base-color: #d2d2d2;

.body {
    .user-name {
        color: #2f88c3;
        font-weight: 500;
    }

    .user-avatar {
        display: inline-block;
        width: 65px;
        height: 65px;
        border: 3px solid @user-avatar-base-color;
        border-radius: 50%;
        background: @user-avatar-base-color;
    }

    .body-root {
        background: #fff;
        border-bottom: 1px solid #e4e5e7;
    }

    .body-reply {
        background: #fff;
        box-shadow: 0 0 20px 0 rgba(0, 0, 0, 0.125);

        .user-avatar {
            position: relative;
            top: -20px;
        }

        .body-header {
            padding-top: 10px;
            padding-bottom: 0;
            border: 0;
        }

        .body-corpus {
            padding-top: 0;
        }
    }

    .body-header,
    .body-actions {
        display: flex;
        flex-direction: row;
        padding: 28px;
        border-bottom: 1px solid #e4e5e7;
        position: -moz-sticky;
    }

    .body-actions {
        padding: 16px 28px;
        justify-content: right;
        font-size: 0.9rem;
    }

    .body-meta-box {
        padding: 10px 0;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        flex: 1;
        margin-left: 20px;
    }

    .body-subject {
        font-weight: 500;
        font-size: 1.4rem;
        margin-bottom: 6px;
    }

    .body-corpus {
        padding: 30px;

        p {
            margin: 20px;
        }
    }

    .replies {
        margin: 40px 30px 20px;
    }
}

.buttons {
    display: inline-block;
    border: 1px solid #e4e5e7;
    border-radius: 6px;
    padding: 8px 12px;
    background: transparent;
    color: inherit;
    cursor: pointer;
    font-weight: 500;

    &.button-trash {
        border: 0;
        padding-left: 30px;
        background-image: url(/assets/images/garbage.svg);
        background-size: 22px auto;
        background-position: left center;
        background-repeat: no-repeat;
    }

    &.button-envelope {
        border: 0;
        padding-left: 30px;
        background-image: url(/assets/images/envelope.svg);
        background-size: 22px auto;
        background-position: left center;
        background-repeat: no-repeat;
    }
}
</style>
