<template>
    <div class="settings-corpus">
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
                        <input type="text" id="aws-access-key" value="" />
                    </div>
                    <div class="form-item">
                        <label for="aws-secret-access-key">AWS Secret Access Key</label>
                        <input type="text" id="aws-secret-access-key" value="" />
                    </div>
                </div>
                <div class="form-group">
                    <div class="form-item">
                        <label for="aws-region">AWS Region</label>
                        <input type="text" id="aws-region" value="" />
                    </div>
                    <div class="form-item">
                        <label for="kms_id">AWS KMS ID</label>
                        <input type="text" id="kms_id" value="" />
                    </div>
                </div>
                <div class="form-item">
                    <label for="bucket">Bucket</label>
                    <input type="text" id="bucket" value="" />
                </div>
                <div class="form-item">
                    <label for="prefix">Prefix</label>
                    <input type="text" id="prefix" value="" />
                </div>
            </form>
        </div>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue } from "vue-property-decorator";

    @Component
    export default class EmailProvider extends Vue {
    }
</script>
