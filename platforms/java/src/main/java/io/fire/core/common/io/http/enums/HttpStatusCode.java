package io.fire.core.common.io.http.enums;

import lombok.Getter;

public enum HttpStatusCode {

    /**
     * HTTP-Status-Codes
     *
     * Notated with their numerical ID and there visual representation
     */

    C_100(100, "Continue"),
    C_101(101, "Switching Protocols"),
    C_200(200, "OK"),
    C_201(201, "Created"),
    C_202(202, "Accepted"),
    C_203(203, "Non-Authoritative Information"),
    C_204(204, "No Content"),
    C_205(205, "Reset Content"),
    C_206(206, "Partial Content"),

    C_300(300, "Multiple Choices"),
    C_301(301, "Moved Permanently"),
    C_302(302, "Found"),
    C_303(303, "See Other"),
    C_304(304, "Not Modified"),
    C_305(305, "Use Proxy"),
    C_307(307, "Temporary Redirect"),

    C_400(400, "Bad Request"),
    C_401(401, "Unauthorized"),
    C_402(402, "Payment Required"),
    C_403(403, "Forbidden"),
    C_404(404, "Not Found"),
    C_405(405, "Method Not Allowed"),
    C_406(406, "Not Acceptable"),
    C_407(407, "Proxy Authentication Required"),
    C_408(408, "Request Time-out"),
    C_409(409, "Conflict"),
    C_410(410, "Gone"),
    C_411(411, "Length Required"),
    C_412(412, "Precondition Failed"),
    C_413(413, "Request Entity Too Large"),
    C_414(414, "Request-URI Too Large"),
    C_415(415, "Unsupported Media Type"),
    C_416(416, "Requested range not satisfiable"),
    C_417(417, "Expectation Failed"),

    C_500(500, "Internal Server Error"),
    C_501(501, "Not Implemented"),
    C_502(502, "Bad Gateway"),
    C_503(503, "Service Unavailable"),
    C_504(504, "Gateway Time-out"),
    C_505(505, "HTTP Version not supported extension-code");

    @Getter private int code;
    @Getter private String type;
    HttpStatusCode(int i, String s) {
        this.code = i;
        this.type = s;
    }
}