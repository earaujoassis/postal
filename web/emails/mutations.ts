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
    emailRead(state, publicId: string) {
        let emails: Array<EmailSummary> = state.emails;
        for (let email of emails) {
            if (email.publicId === publicId) {
                email.metadata.read = true;
                break;
            }
        }
        state.emails = emails;
    },
    emailsError(state) {
        state.error = true;
    }
};
