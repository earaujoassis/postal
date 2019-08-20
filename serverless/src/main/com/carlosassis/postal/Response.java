package com.carlosassis.postal;

public class Response {

    private final String message;

    public Response(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
