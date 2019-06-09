package io.fire.core.common.io.http.enums;

import lombok.Getter;

public enum  HttpRequestMethod {

    /**
     * Implemented HTTP request methods
     */

    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    TRACE("TRACE"),
    OPTIONS("OPTIONS"),
    CONNECT("CONNECT"),
    EMIT("EMIT"), // OUT-OF-SPEC! NOT FOR USE WITH HTTP BUT FIREIO
    PATCH("PATCH");

    @Getter private String method;
    HttpRequestMethod(String s) {
        this.method = s;
    }


    /**
     * Checks the validity of a string as a http packet
     *
     * @param content
     * @return
     */
    public static Boolean isHttp(String content) {
        for (HttpRequestMethod value : values()) {
            if (content.startsWith(value.toString())) {
                if (value == EMIT) return false;
                return true;
            }
        }
        return false;
    }
}