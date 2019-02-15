import { Module } from "vuex";

import { getters } from "./getters";
import { actions } from "./actions";
import { mutations } from "./mutations";
import { EmailsState } from "./types";
import { RootState } from "../types";

export const state: EmailsState = {
    folder: "inbox",
    emails: [],
    currentEmail: undefined,
    error: false
};

const namespaced: boolean = true;

export const emails: Module<EmailsState, RootState> = {
    namespaced,
    state,
    getters,
    actions,
    mutations
};
