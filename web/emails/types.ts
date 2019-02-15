declare const moment: any;
declare const DOMParser: any;

export interface EmailCommons {
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
    isRead(): boolean;
    isActive(currentEmail: any): boolean;
}

export class EmailSummary implements EmailCommons {
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
            return "Origin not available";
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
            abstract = "Excerpt not available";
        }

        return abstract;
    }

    relativeTime(): string {
        return moment(new Date(this.sentAt), "YYYYMMDD").fromNow();
    }

    isRead(): boolean {
        return this.metadata.read === true;
    }

    isActive(currentEmail: any): boolean {
        return currentEmail ? currentEmail.publicId === this.publicId : false;
    }
}

export class EmailFull extends EmailSummary {
    to: Array<string>;
    replyTo: string;

    constructor(source: any) {
        super(source);
        this.to = source.to;
        this.replyTo = source.replyTo;
        this.metadata = source.metadata;
    }

    corpus(): string {
        let splitter: string = this.bodyPlain.indexOf("\r\n") > 0 ? "\r\n" : "\n\n";
        let template: string = this.bodyPlain
            .trim()
            .split(splitter)
            .filter(part => part.length > 0)
            .map(part => `<p>${part}</p>`)
            .join("");

        return template;
    }
}

export interface EmailsState {
    emails: Array<EmailSummary>;
    currentEmail?: EmailFull;
    error: boolean;
}
