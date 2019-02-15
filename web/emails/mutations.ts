import { MutationTree } from "vuex";

import { EmailsState, EmailSummary, EmailFull } from "./types";

export const mutations: MutationTree<EmailsState> = {
    emailsLoaded(state, emails: Array<EmailSummary>) {
        state.error = false;
        state.emails = emails;
    },
    currentEmailLoaded(state, email: EmailFull) {
        state.error = false;
        state.currentEmail = email;
    },
    emailsError(state) {
        state.error = true;
    }
};
