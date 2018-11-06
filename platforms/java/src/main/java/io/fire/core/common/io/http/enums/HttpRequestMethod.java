package io.fire.core.common.io.http.enums;

import lombok.Getter;

public enum  HttpRequestMethod {

    GET("GET"),
    HEAD("HEAD"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE"),
    TRACE("TRACE"),
    OPTIONS("OPTIONS"),
    CONNECT("CONNECT"),
    PATCH("PATCH");

    @Getter private String method;
    HttpRequestMethod(String s) {
        this.method = s;
    }

    public static Boolean isHttp(String content) {
        for (HttpRequestMethod value : values()) {
            if (content.startsWith(value.toString())) {
                return true;
            }
        }
        return false;
    }
}