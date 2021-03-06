import { Module } from "vuex";

import { actions } from "./actions";
import { mutations } from "./mutations";
import { EmailsState } from "./types";
import { RootState } from "@/store/types";

const namespaced: boolean = true;

export const state: EmailsState = {
    folder: "inbox",
    emails: [],
    currentEmail: undefined!,
    error: false,
    total: 0,
    allUnread: 0
};

export const emails: Module<EmailsState, RootState> = {
    namespaced,
    state,
    actions,
    mutations
};
