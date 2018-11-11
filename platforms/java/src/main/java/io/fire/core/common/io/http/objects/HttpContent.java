package io.fire.core.common.io.http.objects;

import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpRequestMethod;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import lombok.Getter;
import lombok.Setter;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HttpContent {

    private String opcode = null;
    private Map<String, String> mappedData = new HashMap<>();
    @Getter @Setter
    private String body = "";

    //output
    @Getter private String url;
    @Getter private HttpRequestMethod method;

    //two constructors, one to import a header set and one to create an empty one
    //reader
    public HttpContent(String data) {
        //now, the real part, parsing incoming packets
        String[] lines = data.split("\r\n");
        if (lines.length == 0) throw new Error("Invalid http headers (length = 0");

        //it is save to parse
        this.method = HttpRequestMethod.valueOf(lines[0].split(" ")[0]);
        this.url = lines[0].replace(method.toString() + " ", "").replace(" HTTP/1.1", "");

        //NOW, COOL TIME (reading out the headers)
        String[] headers = Arrays.copyOfRange(lines, 1, lines.length);
        for (String header : headers) {
            //is it the last element that marks the end of the headers? well, maybe!
            if (header.equals("")) {
                break;
            }
            String[] plot = header.split(":");
            if (plot.length == 2) mappedData.put(plot[0], plot[1].replaceFirst(" ",""));
        }
        String[] segments = data.split("\r\n\r\n");
        if (segments.length == 2) body = segments[1];
    }

    //creator
    public HttpContent(HttpContentType mimeType, HttpStatusCode statusCode) {
        opcode = "HTTP/1.1 " + statusCode.getCode() + " " + statusCode.getType();
        setMimeType(mimeType);
        setOrigin("");
        mappedData.put("Server", "Fire-IO by Mindgamesnl");
    }

    public HttpContent() {

    }

    //set opcodde
    public void setOpcode(HttpContentType mimeType, HttpStatusCode statusCode) {
        opcode = "HTTP/1.1 " + statusCode.getCode() + " " + statusCode.getType();
        setMimeType(mimeType);
    }

    //get headers
    public Map<String, String> getHeaders() {
        return mappedData;
    }

    //write as packet
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        if (opcode != null) out.append(opcode).append("\r\n");
        mappedData.forEach((k, v) -> out.append(k).append(": ").append(v).append("\r\n"));
        out.append("\r\n");
        if (!body.equals("")) out.append(body);
        return out.toString();
    }

    //set common headers
    public void setMimeType(HttpContentType contentType) {
        setHeader("Content-Type", contentType.getMimeType());
    }

    public String getHeader(String key) {
        return mappedData.get(key);
    }

    public void setOrigin(String origin) {
        setHeader("Origin", origin);
    }

    //as buffer
    public ByteBuffer getBuffer() {
        return ByteBuffer.wrap(toString().getBytes());
    }

    //util
    public void setHeader(String key, String value) {
        if (!key.equals("Server")) mappedData.put(key, value);
    }
}