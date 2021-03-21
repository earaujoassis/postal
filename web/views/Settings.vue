<template>
    <div class="settings-view section-padding">
        <h2 class="section-title"># Settings</h2>
        <UserProfile :profile="state.profile" />
        <EmailProvider :remoteStorage="state.settings.remoteStorage || {}" @save="onSave" />
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue } from "vue-property-decorator";
    import { State, Action } from "vuex-class";

    import EmailProvider from "@/components/EmailProvider.vue";
    import UserProfile from "@/components/UserProfile.vue";

    import { SettingsState, RemoteStorage, Settings as ISettings } from "@/store/modules/settings/types";

    const namespace: string = "settings";
    const emptyRemoteStorage: RemoteStorage = {
        accessKey: "",
        secretAccessKey: "",
        kmsKey: "",
        bucketName: "",
        bucketPrefix: ""
    }

    @Component({
        components: {
            EmailProvider,
            UserProfile
        }
    })
    export default class Settings extends Vue {
        @State(namespace) state!: SettingsState;
        @Action("fetchSettings", { namespace }) fetchSettings: any;
        @Action("updateSettings", { namespace }) updateSettings: any;

        mounted() {
            this.fetchSettings();
        }

        onSave(value: RemoteStorage | undefined) {
            if (value) {
                let settings: ISettings = {
                    remoteStorage: Object.assign(emptyRemoteStorage, value)
                };
                this.updateSettings(settings);
            }
        }
    }
</script>

<style lang="less" scoped>
.settings-view {
    flex: 1 0;
    background-color: #F7F9FB;
    overflow-y: scroll;
}
</style>

<style lang="less">
.settings-corpus {
    background-color: #fff;
    box-shadow: 0 0 8px rgba(0,0,0,0.05);

    & ~ & {
        margin-top: 28px;
    }
}
.settings-corpus-header {
    display: flex;
    flex-direction: row;
    align-items: center;
    justify-content: center;
    background-color: #fff;
    padding: 14px 30px;
    min-height: 30px;
    box-shadow: 0 1px 6px rgba(0,0,0,0.09);
    border-bottom: 2px solid #D9DFE7;

    .subsection-title {
        flex: 1 0;
        vertical-align: middle;
    }

    .subsection-actions {
        flex: 1 0;
        text-align: right;

        .button-save,
        .button-cancel {
            padding: 8px 16px;
            margin: 0 0 0 12px;
            cursor: pointer;
        }

        .button-save {
            border: 1px solid #1991EB;
            border-radius: 4px;
            background-color: #1991EB;
            color: #fff;
        }

        .button-cancel {
            color: #1991EB;
            background-color: transparent;
            border: 1px solid transparent;
        }
    }
}
.settings-corpus-body {
    padding: 30px 30px;
    display: flex;
}
</style>
