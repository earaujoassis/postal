export interface UserProfile {
    fullName: string;
    email: string;
}

export interface RemoteStorage {
    accessKey: string;
    secretAccessKey: string;
    kmsKey: string;
    bucketName: string;
    bucketPrefix: string;
}

export interface Settings {
    remoteStorage?: RemoteStorage;
}

export interface SettingsState {
    profile?: UserProfile;
    settings?: Settings;
    error: boolean;
}
