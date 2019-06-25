package io.fire.core.common.io.socket.interfaces;

import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.common.io.http.objects.HttpContent;
import io.fire.core.common.io.socket.OpHandle;

import java.io.IOException;
import java.io.Serializable;

public class Packager extends HttpContent implements Serializable {

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

    public Packager(OpHandle opHandle) throws IOException {
        super(HttpContentType.EMIT, HttpStatusCode.C_100);
        setIsResponse(false);
        setOpcode(HttpStatusCode.C_100);
        setHeader("f-has-packet", "no");
        setHeader("f-is-internal", "yes");
        setHeader("f-has-string", "no");
        setHeader("f-op-handle", opHandle.toString());
    }

    public Packager(String fromPacket) {
        super(fromPacket);
    }

    public Boolean hasPacketBody() {
        return getHeader("f-has-packet").equals("yes");
    }

    public OpHandle getOpHandle() {
        String key = getHeader("f-op-handle");
        if (key.equals("")) return OpHandle.NONE;
        return OpHandle.valueOf(key);
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
