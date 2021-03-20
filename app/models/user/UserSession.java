package models.user;

import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.io.IOException;

import models.SqlField;

@JsonPropertyOrder(alphabetic=false)
public class UserSession {

    public final static String ENTITY_NAME = "user_session";

    public static class Attributes {
        public final static String ID = "id";
        public final static String USER_ID = "user_id";
        public final static String ACCESS_TOKEN = "access_token";
        public final static String EXPIRES_IN = "expires_in";
        public final static String REFRESH_TOKEN = "refresh_token";
        public final static String SCOPE = "scope";
        public final static String TOKEN_TYPE = "token_type";
        public final static String INVALIDATED = "invalidated";
    }

    @JsonIgnore
    @SqlField(name = Attributes.ID, settable = false)
    public Integer _id;

    @JsonIgnore
    @SqlField(name = Attributes.USER_ID)
    public final Integer userId;

    @JsonIgnore
    @SqlField(name = Attributes.ACCESS_TOKEN)
    public final String accessToken;

    @JsonIgnore
    @SqlField(name = Attributes.EXPIRES_IN)
    public final Integer expiresIn;

    @JsonIgnore
    @SqlField(name = Attributes.REFRESH_TOKEN)
    public final String refreshToken;

    @JsonIgnore
    @SqlField(name = Attributes.SCOPE)
    public final String scope;

    @JsonIgnore
    @SqlField(name = Attributes.TOKEN_TYPE)
    public final String tokenType;

    @JsonIgnore
    @SqlField(name = Attributes.INVALIDATED)
    public final boolean invalidated;

    public UserSession(final Integer userId,
                       final String accessToken,
                       final Integer expiresIn,
                       final String refreshToken,
                       final String scope,
                       final String tokenType) {
        this._id = null;
        this.userId = userId;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.scope = scope;
        this.tokenType = tokenType;
        this.invalidated = false;
    }

    public UserSession(Object hash) {
        Map<String, Object> userSession = (Map<String, Object>) hash;
        Object metadata;

        this._id = Integer.valueOf(userSession.get(Attributes.ID).toString());
        this.userId = Integer.valueOf(userSession.get(Attributes.USER_ID).toString());
        this.accessToken = (String) userSession.get(Attributes.ACCESS_TOKEN);
        this.expiresIn = Integer.valueOf(userSession.get(Attributes.EXPIRES_IN).toString());
        this.refreshToken = (String) userSession.get(Attributes.REFRESH_TOKEN);
        this.scope = (String) userSession.get(Attributes.SCOPE);
        this.tokenType = (String) userSession.get(Attributes.TOKEN_TYPE);
        this.invalidated = ((Boolean) userSession.get(Attributes.INVALIDATED)).booleanValue();
    }

    public boolean stillValid() {
        return !this.invalidated;
    }

    public final String toString() {
        return String.format("UserSession::%s", this.accessToken);
    }

}
