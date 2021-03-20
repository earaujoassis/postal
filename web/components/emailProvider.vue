<template>
    <div class="settings-corpus" v-if="remoteStorage">
        <div class="settings-corpus-header">
            <h3 class="subsection-title">AWS S3 / Email messages</h3>
            <div class="subsection-actions">
                <button @click="emitSave" class="button-save">Save</button>
            </div>
        </div>
        <div class="settings-corpus-body">
            <form @submit="emitSave" class="form" action="." method="POST">
                <div class="form-group">
                    <div class="form-item">
                        <label for="aws-access-key">AWS Access Key</label>
                        <input type="text" id="aws-access-key" v-model="remoteStorageForm.accessKey" />
                    </div>
                    <div class="form-item">
                        <label for="aws-secret-access-key">AWS Secret Access Key</label>
                        <input type="password" id="aws-secret-access-key" v-model="remoteStorageForm.secretAccessKey" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="form-item">
                        <label for="aws-region">AWS Region</label>
                        <input disabled type="text" id="aws-region" value="us-east-1" />
                    </div>
                    <div class="form-item">
                        <label for="kms_key">AWS KMS ID</label>
                        <input type="password" id="kms_key" v-model="remoteStorageForm.kmsKey" />
                    </div>
                </div>
                <div class="form-item">
                    <label for="bucket">Bucket</label>
                    <input type="text" id="bucket" v-model="remoteStorageForm.bucketName" />
                </div>
                <div class="form-item">
                    <label for="prefix">Prefix</label>
                    <input type="text" id="prefix" v-model="remoteStorageForm.bucketPrefix" />
                </div>
            </form>
        </div>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue, Prop, ModelSync } from "vue-property-decorator";

    import { RemoteStorage } from "@/store/modules/settings/types";

    @Component
    export default class EmailProvider extends Vue {
        @Prop() remoteStorage?: RemoteStorage | undefined;
        @ModelSync('remoteStorage', 'remoteStorage') remoteStorageForm!: RemoteStorage;

        emitSave() {
            this.$emit('save', this.remoteStorageForm);
        }
    }
</script>
