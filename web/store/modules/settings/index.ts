import { Module } from "vuex";

import { actions } from "./actions";
import { mutations } from "./mutations";
import { SettingsState, Settings } from "./types";
import { RootState } from "@/store/types";

const namespaced: boolean = true;

export const initialSettings: Settings = {
    remoteStorage: undefined!
}

export const state: SettingsState = {
    profile: undefined!,
    settings: initialSettings,
    error: false
};

export const settings: Module<SettingsState, RootState> = {
    namespaced,
    state,
    actions,
    mutations
};
