import { ActionTree } from "vuex";

import { EmailsState, EmailSummary, EmailFull } from "./types";
import { RootState } from "../types";

export const actions: ActionTree<EmailsState, RootState> = {
    fetchEmailsData({ commit }): any {
        fetch("/api/emails")
            .then(response => response.json())
            .then((entries: Array<any>) => {
                const emails: Array<EmailSummary> = entries.map((entry: any) => new EmailSummary(entry));
                commit("emailsLoaded", emails);
            }, error => {
                console.error(error);
                commit("emailsError");
            });
    },

    fetchEmailData({ commit }, publicId): any {
        fetch(`/api/emails/${publicId}`)
            .then(response => response.json())
            .then((entry: any) => {
                const email: EmailFull = new EmailFull(entry);
                commit("currentEmailLoaded", email);
            }, error => {
                console.error(error);
                commit("emailsError");
            });
    },

    markEmailsAsRead({ commit }, publicId): any {
        fetch(`/api/emails/${publicId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json", "X-Requested-With": "fetch" },
            body: JSON.stringify({
                email: {
                    metadata: {
                        read: true
                    }
                }
            })
        })
        .then(function(response) {
            if (response.status >= 200 && response.status < 300) {
                commit("emailRead", publicId);
            } else {
                throw new Error(`server responded with status: ${response.status}`);
            }
        })
        .catch(function(error) {
            console.error(error);
            commit("emailsError");
        });
    }
};
