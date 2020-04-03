package actions;

import play.mvc.Action;
import play.mvc.*;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@With(AuthenticationAction.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authentication {

    boolean enforce() default false;
    boolean json() default true;

}
