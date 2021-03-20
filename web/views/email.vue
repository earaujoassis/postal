<template>
    <div class="email-view">
        <div class="listing">
            <div class="listing-header">
                <h3>1&ndash;10 of {{ state.total }} messages</h3>
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
        </div>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue } from "vue-property-decorator";
    import { State, Action } from "vuex-class";

    import { EmailsState } from "@/store/modules/emails/types";

    const namespace: string = "emails";

    @Component
    export default class Email extends Vue {
        @State(namespace) state!: EmailsState;
        @Action("fetchStatusData", { namespace }) fetchStatusData: any;
        @Action("fetchEmailsData", { namespace }) fetchEmailsData: any;

        mounted() {
            this.fetchStatusData();
            this.fetchEmailsData();
        }

        openEmailView(publicId: string) {
            this.$router.push(`/${this.state.folder}/email/${publicId}`);
        }
    }
</script>

<style lang="less" scoped>
.email-view {
    display: flex;
    flex-direction: row;
    flex: 1 0;
}

.listing {
    position: relative;
    z-index: 100;
    flex: 2 0;
    max-width: 26vw;
    border-right: 2px solid #D9DFE7;
    box-shadow: 0 4px 8px rgba(0,0,0,0.025);
    overflow-y: scroll;
    overflow-x: hidden;
}

.listing-header {
    padding: 18px 20px;
    border-bottom: 2px solid #D9DFE7;
    font-size: inherit;
}

.entry-box {
    padding: 20px 22px;
    border-bottom: 1px solid #dee2e7;
    border-left: 5px solid #f25653;
    line-height: 1.4;
    cursor: pointer;
    transition: border-left 0.25s;

    &:hover,
    &:focus,
    &:focus-within,
    &.active {
        box-shadow: inset 2px 0 20px 0 rgba(41, 46, 52, 0.0625);
        background: #fff;
        transition: border-left 0.25s;
    }

    &.read {
        background: #fff;
        border-left: 0;
        transition: border-left 0.25s;
    }

    &.active,
    &.active.read {
        border-left: 5px solid #5a6e8f;
        transition: border-left 0.25s;
    }

    .entry-header {
        display: flex;
        flex-direction: row;
        justify-content: space-between;
    }

    .entry-author {
        font-size: 1rem;
    }

    .entry-datetime {
        align-self: flex-end;
        color: #2f88c3;
    }

    .entry-subject {
        font-size: 1rem;
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
    }

    .entry-excerpt {
        font-weight: 300;
        white-space: nowrap;
        text-overflow: ellipsis;
        overflow: hidden;
    }
}

.body {
    flex: 3 0;
    overflow-y: scroll;
    overflow-x: hidden;
    padding-left: 5px;
    background: #fff;
}
</style>
