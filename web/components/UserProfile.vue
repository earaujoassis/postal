<template>
    <div class="settings-corpus">
        <div class="settings-corpus-header">
            <h3 class="subsection-title">User profile</h3>
        </div>
        <div class="settings-corpus-body">
            <div class="form">
                <div class="form-group">
                    <div class="form-item">
                        <label for="full-name">Full name</label>
                        <input disabled type="text" id="full-name" value="" />
                    </div>
                    <div class="form-item">
                        <label for="email">E-mail (user account)</label>
                        <input disabled type="text" id="email" value="" />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue } from "vue-property-decorator";

    @Component
    export default class UserProfile extends Vue {
    }
</script>
