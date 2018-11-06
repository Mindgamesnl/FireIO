package io.fire.core.common.io.http.enums;

import lombok.Getter;

public enum HttpContentType {

    AAC_AUDIO("audio/aac"),
    CSS("text/css"),
    HTML("text/html"),
    TEXT("text/plain"),
    JAVA_JAR("application/java-archive"),
    JAVA_SCRIPT("application/javascript"),
    JSON("application/json"),
    TYPE_SCRIPT("application/typescript"),
    XHTML("application/xhtml+xml"),
    XML("application/xml");

    @Getter private String mimeType;

    HttpContentType(String s) {
        this.mimeType = s;
    }
}