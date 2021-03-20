<template>
    <div class="empty-message-view section-padding">
        <img src="/assets/images/undraw_Taken_re_yn20.svg" width="300" alt="Empty message selection" />
        <h2 class="section-title">No message is selected</h2>
    </div>
</template>

<script lang="ts">
    import Component from "vue-class-component";
    import { Vue } from "vue-property-decorator";

    @Component
    export default class EmptyMessage extends Vue {
    }
</script>

<style lang="less" scoped>
.empty-message-view {
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    align-self: center;
    height: calc(100vh - 100px);
    text-align: center;

    .section-title {
        margin-top: 40px;
    }
}
</style>
