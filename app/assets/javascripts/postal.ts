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

    origin(): string;
    abstract(): string;
    relativeTime(): string;
}

class EmailSummary implements EmailCommons {
    publicId: string;
    sentAt: number;
    subject: string;
    from: string;
    fromPersonal: string;
    bodyPlain: string;
    bodyHTML: string;

    constructor(source: any) {
        this.publicId = source.publicId;
        this.sentAt = source.sentAt;
        this.subject = source.subject;
        this.from = source.from;
        this.fromPersonal = source.fromPersonal;
        this.bodyPlain = source.bodyPlain;
        this.bodyHTML = source.bodyHTML;
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
            abstract = this.bodyPlain.trim().slice(0, 100);
            abstract = this.bodyPlain.length > 100 ? abstract + "..." : abstract;
        } else if (this.bodyHTML) {
            let htmlAbstract = this.bodyHTML;
            let text: string = (new DOMParser).parseFromString(htmlAbstract, "text/html").documentElement.textContent;
            abstract = text.trim().slice(0, 100) + "...";
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
    metadata: any;

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
        }
    }
}

const Streams = {
    triggerStream: function(state: State) {
        if (Streams[state.streamName]) {
            Streams[state.streamName](state.payload);
        }
    },

    initial: function() {
        let parentApplier = document.querySelector("div.listing ul.entries");
        while (parentApplier.firstChild) {
            parentApplier.removeChild(parentApplier.firstChild);
        }
        fetch("/api/emails")
            .then(function(response) {
                return response.json();
            })
            .then(function(entries: Array<any>) {
                for (let entry of entries) {
                    entry = new EmailSummary(entry);
                    let entryElement: Element = document.createElement("li");
                    let template: string = `<div id="email-${entry.publicId}" class="entry-box" role="tab" aria-selected="false" aria-controls="body-corpus-id"><div class="entry-header"><p class="entry-author">${entry.fromPersonal}</p><span class="entry-datetime">${entry.relativeTime()}</span></div><h2 class="entry-subject">${entry.subject}</h2><p class="entry-abstract">${entry.abstract()}</p></div>`;
                    entryElement.innerHTML = template;
                    entryElement.firstChild.addEventListener("click", function() {
                        history.pushState({
                            streamName: "showEmail",
                            payload: {
                                publicId: entry.publicId
                            }
                        }, "Postal", `/email/${entry.publicId}`);
                        Streams.triggerStream({
                            streamName: "showEmail",
                            payload: {
                                publicId: entry.publicId,
                                target: entryElement.firstChild
                            }
                        });
                    }, false);
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

        target.setAttribute("aria-selected", "true");
        while (parentApplier.firstChild) {
            parentApplier.removeChild(parentApplier.firstChild);
        }
        fetch(`/api/emails/${payload.publicId}`)
            .then(function(response) {
                return response.json();
            })
            .then(function(email: any) {
                email = new EmailFull(email);
                let entryElement: Element = document.createElement('div');
                entryElement.className = 'body-root';
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
    Streams["initial"]();
});

window.onpopstate = function(event: any) {
    let state: State = event.state;
}
