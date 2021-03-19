import { ActionTree } from "vuex";

import { SettingsState, Settings } from "./types";
import { RootState } from "@/store/types";

export const actions: ActionTree<SettingsState, RootState> = {
    fetchSettingsData({ commit }): any {
        fetch(`/api/settings`)
            .then(response => response.json())
            .then((settings: Settings) => {
                commit("settingsLoaded", settings);
            }, error => {
                console.error(error);
                commit("settingsError");
            });
    },

    updateSettingsData({ commit }, metadata): any {
        fetch(`/api/settings`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json", "X-Requested-With": "fetch" },
            body: JSON.stringify({
                user: {
                    metadata: metadata
                }
            })
        })
        .then(function(response) {
            if (response.status >= 200 && response.status < 300) {
                return response.json();
            } else {
                throw new Error(`server responded with status: ${response.status}`);
            }
        })
        .then((settings: Settings) => {
            commit("settingsLoaded", settings);
        }, error => {
            console.error(error);
            commit("settingsError");
        });
    },
};
