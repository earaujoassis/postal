import { MutationTree } from "vuex";

import { SettingsState } from "./types";

export const mutations: MutationTree<SettingsState> = {
    settingsLoaded(state, data: any) {
        state.error = false;
        state.settings = data.settings;
        state.profile = data.profile;
    },
    settingsError(state) {
        state.error = true;
    }
};
