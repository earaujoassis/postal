package services;

import javax.inject.Singleton;
import javax.inject.Inject;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import utils.StringUtils;
import utils.HttpHelper;
import utils.HttpHelperException;
import services.AppConfig;

@Singleton
public class OAuthService {

    private String redirectUri;
    private String clientKey;
    private String clientSecret;
    private String clientAuthorizationToken;
    private String baseUrl;
    private String authorizeUrl;
    private final String scope = "public";

    @Inject
    public OAuthService(AppConfig conf) {
        this.redirectUri = StringUtils.encodeUriComponent(conf.getValue("space.redirect_uri"));
        this.clientKey = conf.getValue("space.client_key");
        this.clientSecret = conf.getValue("space.client_secret");
        this.clientAuthorizationToken = Base64.getEncoder().encodeToString(
            String.format("%s:%s", this.clientKey, this.clientSecret).getBytes());
        this.baseUrl = conf.getValue("space.base_url");
        this.authorizeUrl = String.format("%s/authorize?client_id=%s&redirect_uri=%s&response_type=code&scope=%s&state=",
            this.baseUrl, this.clientKey, this.redirectUri, this.scope);
    }

    public String getAuthorizeUrl() {
        return this.authorizeUrl;
    }

    public String getBaseUrl() {
        return this.baseUrl;
    }

    public String retrieveAccessToken(String code) throws OAuthServiceException {
        String url = String.format("%s/token", this.baseUrl);
        Map<String, String> header = this.commonHeader();
        Map<String, String> formData = this.retrieveAccessTokenFormData(code);
        String content;

        try {
            content = HttpHelper.doPost(url, header, formData);
        } catch (HttpHelperException err) {
            throw new OAuthServiceException(err.getMessage());
        }

        return content;
    }

    private Map<String, String> commonHeader() {
        Map<String, String> header = new HashMap<>();

        header.put("Authorization", String.format("Basic %s", this.clientAuthorizationToken));
        header.put("Accept", "application/vnd.space.v1+json");
        header.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

        return header;
    }

    private Map<String, String> retrieveAccessTokenFormData(String code) {
        Map<String, String> arguments = new HashMap<>();

        arguments.put("grant_type", "authorization_code");
        arguments.put("code", code);
        arguments.put("redirect_uri", this.redirectUri);

        return arguments;
    }

}
