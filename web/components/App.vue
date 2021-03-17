<template>
  <div role="main" class="root">
    <div class="sidebar">
      <div>
        <h1 class="branding">
          <img src="//cdn.quatrolabs.com/quatrolabs-logo-white@2x.png" width="40" alt="qL" />
          <span>Postal</span>
        </h1>
        <ul class="menu">
          <li><router-link class="inbox" to="/">Inbox <span class="counter">{{ state.unread }}</span></router-link></li>
          <li><router-link class="all-mail" to="/all-mail">All mail</router-link></li>
          <li><router-link class="sent" to="/sent">Sent</router-link></li>
          <li><router-link class="drafts" to="/drafts">Drafts</router-link></li>
          <li><router-link class="trash" to="/trash">Trash</router-link></li>
          <li><a href="/signout">Sign-out</a></li>
        </ul>
      </div>
      <footer class="footer">
        <p><a href="//quatrolabs.com" target="_blank">quatroLABS</a> &copy; 2016-present</p>
      </footer>
    </div>
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
            role="tab">
            <div class="entry-header">
              <p class="entry-author">{{ entry.origin() }}</p>
              <span class="entry-datetime">{{ entry.relativeTime() }}</span>
            </div>
            <h2 class="entry-subject">{{ entry.subject }}</h2>
            <p class="entry-excerpt">{{ entry.excerpt() }}</p></div>
        </li>
      </ul>
    </div>
    <div class="body" id="body-corpus-id">
      <router-view v-bind:key="$route.fullPath"></router-view>
    </div>
  </div>
</template>

<script lang="ts">
  import Component from "vue-class-component";
  import { Vue } from "vue-property-decorator";
  import { State, Action } from "vuex-class";

  import { EmailsState } from "../emails/types";

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
