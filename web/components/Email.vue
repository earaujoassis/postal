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
  import { Component, Watch, Vue } from "vue-property-decorator";
  import { State, Action, Getter } from "vuex-class";

  import { EmailsState } from "../emails/types";

  const namespace: string = "emails";

  @Component
  export default class App extends Vue {
      @State("emails") state!: EmailsState;
      @Action("fetchEmailData", { namespace }) fetchEmailData: any;
      @Action("markEmailsAsUnread", { namespace }) markEmailsAsUnread: any;
      @Action("markEmailsAsRead", { namespace }) markEmailsAsRead: any;

      mounted() {
        this.fetchEmailData(this.$route.params.id);
      }

      @Watch("$route")
      onRouterChange(to: any, from: any) {
        this.fetchEmailData(to.params.id);
      }

      @Watch("state", { deep: true })
      markAsRead() {
        if (this.state.currentEmail) {
          let emailMetadata: any = this.state.currentEmail.metadata;
          if (emailMetadata.read === false) {
            this.markEmailsAsRead(this.$route.params.id);
          }
        }
      }

      markAsUnread() {
        this.markEmailsAsUnread(this.$route.params.id);
      }

      fixIFrameHeight() {
        let ref: any = this.$refs["currentEmailIFrame"];
        ref.style.height = `${ref.contentWindow.document.body.scrollHeight}` + "px";
      }
  }
</script>
