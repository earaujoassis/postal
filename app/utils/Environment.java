package utils;

import java.util.Optional;

public class Environment {

    public static String currentEnvironment() {
        return Optional.ofNullable(System.getenv("POSTAL_ENV")).orElse("development");
    }

}
