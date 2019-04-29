package io.fire.core.server.modules.http.objects;

import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.common.io.http.objects.HttpContent;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

public class Response {

    private Consumer<ByteBuffer> onFinish;
    private HttpContent httpContent;
    private boolean hasSend = false;

    public  Response(Consumer<ByteBuffer> onFinish) {
        this.onFinish = onFinish;
        this.httpContent = new HttpContent();
    }

    public void setOrigin(String origin) {
        this.httpContent.setOrigin(origin);
    }

    public void setHeader(String key, String value) {
        this.httpContent.setHeader(key, value);
    }

    public void setResponseType(HttpContentType type) {
        this.httpContent.setMimeType(type);
    }

    public void finish(String data) {
        this.httpContent.setBody(data);
        send();
    }

    public void send(String data) {
        this.httpContent.setBody(data);
        send();
    }

    public HttpContent getPacket() {
        return this.httpContent;
    }

    public void setStatus(HttpStatusCode status) {
        this.httpContent.setStatusCode(status);
    }

    private void send() {
        if (hasSend) return;
        hasSend = true;
        onFinish.accept(httpContent.getBuffer());
    }

}
