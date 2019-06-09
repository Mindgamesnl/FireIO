package io.fire.core.common.io.socket.interfaces;

import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.common.io.http.objects.HttpContent;

import java.io.IOException;

public class Packager extends HttpContent {

    public Packager(String channel, Packet packet) throws IOException {
        super(HttpContentType.EMIT, HttpStatusCode.C_100);
        setIsResponse(false);
        setOpcode(HttpStatusCode.C_100);
        setHeader("f-channel", channel);
        setBody(packet);
    }

}
