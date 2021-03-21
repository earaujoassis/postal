import { ActionTree } from "vuex";

import { EmailsState, EmailSummary, EmailFull } from "./types";
import { RootState } from "@/store/types";

export const actions: ActionTree<EmailsState, RootState> = {
    updateCurrentFolder({ commit }, folder: string): any {
        commit("updateFolder", folder);
    },

    fetchStatus({ commit }): any {
        fetch("/api/emails/status")
            .then(response => response.json())
            .then((status: any) => {
                commit("statusLoaded", status);
            }, error => {
                console.error(error);
                commit("emailsError");
            });
    },

    fetchEmails({ commit }, folder: string): any {
        let path = folder == 'all-mail' ? "/api/emails" : `/api/emails?folder=${folder}`;
        fetch(path)
            .then(response => response.json())
            .then(({ emails, total }) => {
                const summaries: Array<EmailSummary> = emails.map((entry: any) => new EmailSummary(entry));
                commit("emailsLoaded", { emails: summaries, total });
            }, error => {
                console.error(error);
                commit("emailsError");
            });
    },

    fetchEmail({ commit }, publicId): any {
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
    },

    markEmailsAsUnread({ commit }, publicId): any {
        fetch(`/api/emails/${publicId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json", "X-Requested-With": "fetch" },
            body: JSON.stringify({
                email: {
                    metadata: {
                        read: false
                    }
                }
            })
        })
        .then(function(response) {
            if (response.status >= 200 && response.status < 300) {
                commit("emailUnread", publicId);
            } else {
                throw new Error(`server responded with status: ${response.status}`);
            }
        })
        .catch(function(error) {
            console.error(error);
            commit("emailsError");
        });
    },

    moveEmailToFolder({ commit }, { publicId, folder }): any {
        fetch(`/api/emails/${publicId}`, {
            method: "PATCH",
            headers: { "Content-Type": "application/json", "X-Requested-With": "fetch" },
            body: JSON.stringify({
                email: {
                    metadata: {
                        folder: folder
                    }
                }
            })
        })
        .then(function(response) {
            if (response.status >= 200 && response.status < 300) {
                commit("emailMoved", { publicId, folder });
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
