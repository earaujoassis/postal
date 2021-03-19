<template>
    <div class="settings-corpus" v-if="remoteStorage">
        <div class="settings-corpus-header">
            <h3 class="subsection-title">AWS S3 / Email messages</h3>
            <div class="subsection-actions">
                <button class="button-cancel">Cancel</button>
                <button class="button-save">Save</button>
            </div>
        </div>
        <div class="settings-corpus-body">
            <form class="form" action="." method="POST">
                <div class="form-group">
                    <div class="form-item">
                        <label for="aws-access-key">AWS Access Key</label>
                        <input type="text" id="aws-access-key" :value="remoteStorage.accessKey" />
                    </div>
                    <div class="form-item">
                        <label for="aws-secret-access-key">AWS Secret Access Key</label>
                        <input type="text" id="aws-secret-access-key" :value="remoteStorage.secretAccessKey" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="form-item">
                        <label for="aws-region">AWS Region</label>
                        <input disabled type="text" id="aws-region" value="us-east-1" />
                    </div>
                    <div class="form-item">
                        <label for="kms_key">AWS KMS ID</label>
                        <input type="text" id="kms_key" :value="remoteStorage.kmsKey" />
                    </div>
                </div>
                <div class="form-item">
                    <label for="bucket">Bucket</label>
                    <input type="text" id="bucket" :value="remoteStorage.bucketName" />
                </div>
                <div class="form-item">
                    <label for="prefix">Prefix</label>
                    <input type="text" id="prefix" :value="remoteStorage.bucketPrefix" />
                </div>
            </form>
        </div>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue, Prop } from "vue-property-decorator";

    import { RemoteStorage } from "@/store/modules/settings/types";

    @Component
    export default class EmailProvider extends Vue {
        @Prop() remoteStorage?: RemoteStorage;
    }
</script>
