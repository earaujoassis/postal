package actions;

import play.libs.typedmap.TypedKey;

import models.user.User;

public class AuthenticationAttrs {

    public static final TypedKey<User> USER = TypedKey.<User>create("user");

}
