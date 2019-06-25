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
        setHeader("f-has-packet", "yes");
        setHeader("f-is-internal", "no");
        setHeader("f-has-string", "no");
        setBody(packet);
    }

    public Packager(String channel, String data) {
        super(HttpContentType.EMIT, HttpStatusCode.C_100);
        setIsResponse(false);
        setOpcode(HttpStatusCode.C_100);
        setHeader("f-channel", channel);
        setHeader("f-has-packet", "no");
        setHeader("f-is-internal", "no");
        setHeader("f-has-string", "yes");
        setBody(data);
    }

    public Packager(Packet packet) throws IOException {
        super(HttpContentType.EMIT, HttpStatusCode.C_100);
        setIsResponse(false);
        setOpcode(HttpStatusCode.C_100);
        setHeader("f-has-packet", "yes");
        setHeader("f-is-internal", "yes");
        setHeader("f-has-string", "no");
        setBody(packet);
    }

    public Packager() throws IOException {
        super(HttpContentType.EMIT, HttpStatusCode.C_100);
        setIsResponse(false);
        setOpcode(HttpStatusCode.C_100);
        setHeader("f-has-packet", "no");
        setHeader("f-is-internal", "yes");
        setHeader("f-has-string", "no");
    }

    public Packager(String fromPacket) {
        super(fromPacket);
    }

    public Boolean hasPacketBody() {
        return getHeader("f-has-packet").equals("yes");
    }

    public Boolean isInternal() {
        return getHeader("f-is-internal").equals("yes");
    }

    public Boolean hasStringBody() {
        return getHeader("f-has-string").equals("yes");
    }

    public String getChannel() {
        return getHeader("f-channel");
    }

}
