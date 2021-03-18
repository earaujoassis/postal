<template>
    <div class="settings-view section-padding">
        <h2 class="section-title"># Settings</h2>
        <UserProfile />
        <EmailProvider />
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue } from "vue-property-decorator";
    import EmailProvider from "@/components/EmailProvider.vue";
    import UserProfile from "@/components/UserProfile.vue";

    @Component({
        components: {
            EmailProvider,
            UserProfile
        }
    })
    export default class Settings extends Vue {
    }
</script>

<style lang="less" scoped>
.settings-view {
    flex: 1 0;
    background-color: rgba(0,0,0,0.1);
    overflow-y: scroll;
}
</style>

<style lang="less">
.settings-corpus {
    background-color: #fff;
    box-shadow: 0 0 8px rgba(0,0,0,0.025);

    & ~ & {
        margin-top: 28px;
    }
}
.settings-corpus-header {
    display: flex;
    flex-direction: row;
    align-items: center;
    background-color: rgba(0,0,0,0.05);
    padding: 14px 30px;
    min-height: 30px;
    box-shadow: 0 2px 8px rgba(0,0,0,0.09);

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
            border: 1px solid #26ADE4;
            border-radius: 4px;
            background-color: #26ADE4;
            color: #fff;
        }

        .button-cancel {
            color: #26ADE4;
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
