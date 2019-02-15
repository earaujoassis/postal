<template>
  <div role="main" class="root">
    <div class="sidebar">
      <div>
        <h1 class="branding">
          <img src="//cdn.quatrolabs.com/quatrolabs-logo-white@2x.png" width="40" alt="qL" />
          <span>Postal</span>
        </h1>
        <ul class="menu">
          <li><a class="active" href="/">Inbox <span class="counter">{{ unReadEmails }}</span></a></li>
          <li><a href="/all-mail">All mail</a></li>
          <li><a href="/sent">Sent</a></li>
          <li><a href="/drafts">Drafts</a></li>
          <li><a href="/trash">Trash</a></li>
        </ul>
      </div>
      <footer class="footer">
        <p><a href="//quatrolabs.com" target="_blank">quatroLABS</a> &copy; 2019</p>
      </footer>
    </div>
    <div class="listing">
      <ul class="entries" role="tablist">
        <li v-for="entry in state.emails">
          <div
            v-bind:id="entry.publicId"
            v-bind:class="{ 'entry-box': true, read: entry.isRead(), active: entry.isActive(state.currentEmail) }"
            v-on:click="fetchEmailData(entry.publicId)"
            aria-selected="false"
            aria-controls="body-corpus-id"
            role="tab">
            <div class="entry-header">
              <p class="entry-author">{{ entry.origin() }}</p>
              <span class="entry-datetime">{{ entry.relativeTime() }}</span>
            </div>
            <h2 class="entry-subject">{{ entry.subject }}</h2>
            <p class="entry-abstract">{{ entry.abstract() }}</p></div>
        </li>
      </ul>
    </div>
    <div class="body" id="body-corpus-id">
      <div v-if="state.currentEmail" class="body-header">
        <span class="user-avatar"></span>
        <div class="body-meta-box">
          <h2 class="body-subject">{{ state.currentEmail.subject }}</h2>
          <p><span class="user-name">{{ state.currentEmail.origin() }}</span> to {{ state.currentEmail.to[0] }}</p>
        </div>
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
                <p><span class="user-name">Carlos Assis</span> to wired@@newsletters.wired.com</p>
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
  import { Component, Prop, Vue } from "vue-property-decorator";
  import { State, Action, Getter } from "vuex-class";

  import { EmailsState } from "../emails/types";

  const namespace: string = "emails";

  @Component
  export default class App extends Vue {
      @State("emails") state!: EmailsState;
      @Action("fetchData", { namespace }) fetchData: any;
      @Action("fetchEmailData", { namespace }) fetchEmailData: any;

      mounted() {
        this.fetchData();
      }

      fixIFrameHeight() {
        let ref: any = this.$refs["currentEmailIFrame"];
        ref.style.height = `${ref.contentWindow.document.body.scrollHeight}` + "px";
      }

      get unReadEmails() {
        let counter = 0;

        if (this.state && this.state.emails && this.state.emails.length) {
          const entries: Array<any> = this.state.emails;
          counter = entries.reduce((accumulator, entry) => entry.metadata.read ? accumulator : accumulator + 1, 0);
        }

        return counter;
      }
  }
</script>
