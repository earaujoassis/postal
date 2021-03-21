<template>
    <div role="main" class="root">
        <div class="sidebar">
            <div>
                <h1 class="branding">
                    <img src="//cdn.quatrolabs.com/quatrolabs-logo-small@2x.png" width="40" alt="qL" />
                    <span>Postal</span>
                </h1>
                <ul class="menu">
                    <li><router-link class="inbox" to="/inbox">Inbox</router-link></li>
                    <li><router-link class="all-mail" to="/all-mail">All mail <span class="counter">{{ state.unread }}</span></router-link></li>
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

    import { EmailsState } from "@/store/modules/emails/types";

    const namespace: string = "emails";

    @Component
    export default class App extends Vue {
        @State("emails") state!: EmailsState;
        @Action("fetchStatus", { namespace }) fetchStatus: any;

        mounted() {
            this.fetchStatus();
        }
    }
</script>

<style lang="less" scoped>
.root {
    height: 100vh;
    width: 100vw;
    display: flex;
    flex-direction: row;
}

.branding {
    display: flex;
    flex-direction: row;
    align-items: center;
    height: 40px;
    padding: 40px 28px 20px;

    > span {
        margin-left: 10px;
    }
}

.sidebar {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    flex: 1 0;
    max-width: 20vw;
    min-width: 260px;
    background: #fff;
    border-right: 2px solid #D9DFE7;
    color: #354053;
    overflow: hidden;

    .counter {
        color: #fcb2b2;
    }

    .menu {
        padding: 12px 28px;
        font-weight: 500;

        > li {
            padding: 8px 0;

            .active {
                color: #9DAABF;
                background-color: #EFF3F7;
            }

            a {
                position: relative;
                padding: 8px 10px;
                border-radius: 4px;
                display: flex;
                flex-direction: row;
                justify-content: space-between;
                color: #637385;
                background: transparent;
                transition: background 0.5s;
                padding-left: 36px;

                &:hover,
                &:focus {
                    color: #9DAABF;
                    background: #EFF3F7;
                    transition: background 0.5s;
                }

                &.inbox:before {
                    content: "";
                    position: absolute;
                    top: 0;
                    bottom: 0;
                    left: 0;
                    -webkit-mask-image: url(/assets/images/inbox.svg);
                    -webkit-mask-size: 22px auto;
                    -webkit-mask-position: center;
                    -webkit-mask-repeat: no-repeat;
                    mask-image: url(/assets/images/inbox.svg);
                    mask-size: 22px auto;
                    mask-position: center;
                    mask-repeat: no-repeat;
                    background-color: #637385;
                    width: 26px;
                }

                &.sent:before {
                    content: "";
                    position: absolute;
                    top: 0;
                    bottom: 0;
                    left: 0;
                    -webkit-mask-image: url(/assets/images/cursor.svg);
                    -webkit-mask-size: 22px auto;
                    -webkit-mask-position: center;
                    -webkit-mask-repeat: no-repeat;
                    mask-image: url(/assets/images/cursor.svg);
                    mask-size: 22px auto;
                    mask-position: center;
                    mask-repeat: no-repeat;
                    background-color: #637385;
                    width: 26px;
                }

                &.drafts:before {
                    content: "";
                    position: absolute;
                    top: 0;
                    bottom: 0;
                    left: 0;
                    -webkit-mask-image: url(/assets/images/edit.svg);
                    -webkit-mask-size: 21px auto;
                    -webkit-mask-position: center;
                    -webkit-mask-repeat: no-repeat;
                    mask-image: url(/assets/images/edit.svg);
                    mask-size: 21px auto;
                    mask-position: center;
                    mask-repeat: no-repeat;
                    background-color: #637385;
                    width: 26px;
                }

                &.trash:before {
                    content: "";
                    position: absolute;
                    top: 0;
                    bottom: 0;
                    left: 0;
                    -webkit-mask-image: url(/assets/images/garbage.svg);
                    -webkit-mask-size: 22px auto;
                    -webkit-mask-position: center;
                    -webkit-mask-repeat: no-repeat;
                    mask-image: url(/assets/images/garbage.svg);
                    mask-size: 22px auto;
                    mask-position: center;
                    mask-repeat: no-repeat;
                    background-color: #637385;
                    width: 26px;
                }
            }
        }
    }

    .footer {
        padding: 16px 28px 26px;
        font-size: 0.8rem;
        font-weight: 500;
    }
}
</style>
