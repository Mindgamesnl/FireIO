package io.fire.core.common.io.http.enums;

import lombok.Getter;

public enum HttpContentType {

    /**
     * Most used HTTP content types
     * Notated with their HTTP mime type
     */

    AAC_AUDIO("audio/aac"),
    CSS("text/css"),
    HTML("text/html"),
    TEXT("text/plain"),
    JAVA_JAR("application/java-archive"),
    JAVA_SCRIPT("application/javascript"),
    JSON("application/json"),
    TYPE_SCRIPT("application/typescript"),
    XHTML("application/xhtml+xml"),
    EMIT("socket/fireio"), // OUT-OF-SPEC! NOT FOR USE WITH HTTP BUT FIREIO
    XML("application/xml");

    @Getter private String mimeType;

    HttpContentType(String s) {
        this.mimeType = s;
    }
}