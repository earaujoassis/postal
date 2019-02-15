declare var moment: any;

interface State {
    streamName: string;
    payload: any;
}

interface EmailCommons {
    publicId: string;
    sentAt: number;
    subject: string;
    from: string;
    fromPersonal: string;
    bodyPlain: string;
    bodyHTML: string;
    metadata: any;

    origin(): string;
    abstract(): string;
    relativeTime(): string;
}

class EmailSummary implements EmailCommons {
    static readonly ABSTRACT_CHAR_LIMIT = 64;

    publicId: string;
    sentAt: number;
    subject: string;
    from: string;
    fromPersonal: string;
    bodyPlain: string;
    bodyHTML: string;
    metadata: any;

    constructor(source: any) {
        this.publicId = source.publicId;
        this.sentAt = source.sentAt;
        this.subject = source.subject;
        this.from = source.from;
        this.fromPersonal = source.fromPersonal;
        this.bodyPlain = source.bodyPlain;
        this.bodyHTML = source.bodyHTML;
        this.metadata = source.metadata;
    }

    origin(): string {
        if (this.fromPersonal) {
            return this.fromPersonal;
        } else if (this.from) {
            return this.from;
        } else {
            return '<span class="italic">Unknown origin</span>';
        }
    }

    abstract(): string {
        let abstract: string;

        if (this.bodyPlain) {
            abstract = this.bodyPlain.trim().slice(0, EmailSummary.ABSTRACT_CHAR_LIMIT);
            abstract = this.bodyPlain.length > EmailSummary.ABSTRACT_CHAR_LIMIT ? abstract.trim() + "..." : abstract;
        } else if (this.bodyHTML) {
            let text: string = (new DOMParser).parseFromString(this.bodyHTML, "text/html").documentElement.textContent;
            abstract = text.trim().slice(0, EmailSummary.ABSTRACT_CHAR_LIMIT).trim() + "...";
        } else {
            abstract = '<span class="italic">Not available...</span>';
        }

        return abstract;
    }

    relativeTime(): string {
        return moment(new Date(this.sentAt), "YYYYMMDD").fromNow();
    }
}

class EmailFull extends EmailSummary {
    to: string;
    replyTo: string;

    constructor(source: any) {
        super(source);
        this.to = source.to;
        this.replyTo = source.replyTo;
        this.metadata = source.metadata;
    }

    applyCorpus(el: Element) {
        if (this.bodyHTML) {
            let entryElement: any = document.createElement("iframe");
            entryElement.frameborder = 0;
            entryElement.srcdoc = this.bodyHTML;
            entryElement.style.height = "0px";
            entryElement.scrolling = "no";
            entryElement.onload = function() {
                entryElement.style.height = `${entryElement.contentWindow.document.body.scrollHeight}px`;
            };
            el.appendChild(entryElement);
            return;
        }

        let splitter: string = this.bodyPlain.indexOf("\r\n") > 0 ? "\r\n" : "\n\n";
        let template: string = this.bodyPlain
            .trim()
            .split(splitter)
            .filter(part => part.length > 0)
            .map(part => `<p>${part}</p>`)
            .join("");
        let entryElement: Element = document.createElement("div");
        entryElement.innerHTML = template;
        el.appendChild(entryElement);
    }

    markAsRead() {
        let publicId: string = this.publicId;
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
                let inboxCounter: Element = document.querySelector("div.sidebar ul.menu span.counter");
                inboxCounter.innerHTML = (parseInt(inboxCounter.innerHTML) - 1).toString();
                document.getElementById(`email-${publicId}`).classList.add("read");
            }
        })
        .catch(function(error) {
            console.error(error);
        });
    }
}

const StreamsManager: any = {
    triggerStream: function(state: State) {
        if (Streams[state.streamName]) {
            Streams[state.streamName](state.payload);
        }
    }
}

const Streams: any = {
    initial: function() {
        let parentApplier: Element = document.querySelector("div.listing ul.entries");
        let inboxCounter: Element = document.querySelector("div.sidebar ul.menu span.counter");
        while (parentApplier.firstChild) {
            parentApplier.removeChild(parentApplier.firstChild);
        }
        fetch("/api/emails")
            .then(response => response.json())
            .then(function(entries: Array<any>) {
                let unreadCounter: number = entries.reduce((accumulator, entry) => entry.metadata.read ? accumulator : accumulator + 1, 0);
                inboxCounter.innerHTML = unreadCounter.toString();
                for (let entry of entries) {
                    entry = new EmailSummary(entry);
                    let entryElement: any = document.createElement("li");
                    let template: string = `<div id="email-${entry.publicId}" class="entry-box" role="tab" aria-selected="false" aria-controls="body-corpus-id"><div class="entry-header"><p class="entry-author">${entry.origin()}</p><span class="entry-datetime">${entry.relativeTime()}</span></div><h2 class="entry-subject">${entry.subject}</h2><p class="entry-abstract">${entry.abstract()}</p></div>`;
                    entryElement.innerHTML = template;
                    entryElement.firstChild.addEventListener("click", function() {
                        history.pushState({
                            streamName: "showEmail",
                            payload: {
                                publicId: entry.publicId
                            }
                        }, "Postal", `/email/${entry.publicId}`);
                        StreamsManager.triggerStream({
                            streamName: "showEmail",
                            payload: {
                                publicId: entry.publicId,
                                target: entryElement.firstChild
                            }
                        });
                    }, false);
                    if (entry.metadata.read === true) {
                        entryElement.firstChild.classList.add("read");
                    }
                    parentApplier.appendChild(entryElement);
                }
            })
            .catch(function(error) {
                console.error(error);
            });
    },

    showEmail: function(payload: any) {
        let parentApplier = document.querySelector("div.body");
        let target: Element = payload.target;

        if (document.querySelector("div.entry-box.active")) {
            document.querySelector("div.entry-box.active").classList.remove("active");
        }

        target.setAttribute("aria-selected", "true");
        target.classList.add("active");
        while (parentApplier.firstChild) {
            parentApplier.removeChild(parentApplier.firstChild);
        }
        fetch(`/api/emails/${payload.publicId}`)
            .then(response => response.json())
            .then(function(email: any) {
                email = new EmailFull(email);
                if (email.metadata.read === false) {
                    email.markAsRead();
                }
                let entryElement: Element = document.createElement("div");
                entryElement.className = "body-root";
                let template: string = `<div class="body-header"><span class="user-avatar"></span><div class="body-meta-box"><h2 class="body-subject">${email.subject}</h2><p><span class="user-name">${email.origin()}</span> to ${email.to}</p></div></div><div class="body-corpus"></div>`;
                entryElement.innerHTML = template;
                email.applyCorpus(entryElement.querySelector("div.body-corpus"));
                parentApplier.appendChild(entryElement);
            })
            .catch(function(error) {
                console.error(error);
            });
    }
}

document.addEventListener("DOMContentLoaded", function() {
    StreamsManager.triggerStream({streamName: "initial"});
});

window.onpopstate = function(event: any) {
    let state: State = event.state;
}
