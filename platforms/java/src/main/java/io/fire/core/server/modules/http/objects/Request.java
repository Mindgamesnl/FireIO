package io.fire.core.server.modules.http.objects;

import io.fire.core.common.io.http.enums.HttpRequestMethod;
import io.fire.core.common.io.http.objects.HttpContent;
import lombok.AllArgsConstructor;

import java.net.InetAddress;
import java.util.Map;

@AllArgsConstructor
public class Request {

    private HttpContent httpContent;
    private Map<String, String> variables;
    private InetAddress address;

    public String getVariable(String key) {
        return variables.get(key);
    }

    public String getBody() {
        return httpContent.getBodyAsString();
    }

    public String getHeader(String key) {
        return httpContent.getHeader(key);
    }

    public String getUrl() {
        return httpContent.getUrl();
    }

    public HttpRequestMethod getMethod() {
        return httpContent.getMethod();
    }

    public InetAddress getAdress() {
        return address;
    }

}
