import { MutationTree } from "vuex";

import { EmailsState, EmailSummary, EmailFull } from "./types";
import router from "../router";

export const mutations: MutationTree<EmailsState> = {
    statusLoaded(state, status: any) {
        state.total = status.total;
        state.unread = status.unread;
    },
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
    emailUnread(state, publicId: string) {
        let emails: Array<EmailSummary> = state.emails;
        for (let email of emails) {
            if (email.publicId === publicId) {
                email.metadata.read = false;
                break;
            }
        }
        state.emails = emails;
        state.currentEmail = undefined;
        router.push(`/${state.folder}/`);
    },
    emailsError(state) {
        state.error = true;
    }
};
