package services;

import javax.inject.Inject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import services.AppConfig;

public class JWTService {

    private String secret;
    private Algorithm algorithm;

    @Inject
    public JWTService(AppConfig conf) {
        this.secret = conf.getValue("jwt.secret");
        this.algorithm = Algorithm.HMAC256(this.secret);
    }

    public String signToken(String id) {
        String token;

        try {
            token = JWT.create()
                .withIssuer("postal")
                .withClaim("sessionId", id)
                .sign(this.algorithm);
        } catch (JWTCreationException e){
            return null;
        }

        return token;
    }

    public boolean isValidToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(this.algorithm)
                .withIssuer("postal")
                .build();
        } catch (JWTVerificationException e){
            return false;
        }

        return true;
    }

    public String getSessionId(String token) {
        DecodedJWT jwt;

        try {
            JWTVerifier verifier = JWT.require(this.algorithm)
                .withIssuer("postal")
                .build();
            jwt = verifier.verify(token);
        } catch (JWTVerificationException e){
            return null;
        }

        return jwt.getClaim("sessionId").asString();
    }

}
