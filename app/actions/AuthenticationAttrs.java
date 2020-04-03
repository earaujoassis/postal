package actions;

import play.libs.typedmap.TypedKey;

import models.User;

public class AuthenticationAttrs {

    public static final TypedKey<User> USER = TypedKey.<User>create("user");

}
