package models.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import models.providers.RemoteStorage;

@JsonPropertyOrder(alphabetic=false)
public class UserMetadata {

    public final RemoteStorage remoteStorage;

    public UserMetadata() {
        this.remoteStorage = null;
    }

    @JsonIgnore
    public boolean hasRemoteStorage() {
        return this.remoteStorage != null;
    }

    @JsonIgnore
    public boolean isValid() {
        if (this.hasRemoteStorage()) {
            return this.remoteStorage.isValid();
        }

        return true;
    }

}
