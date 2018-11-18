package io.fire.core.common.io.api.api;

import io.fire.core.common.io.http.objects.ConnectionInfo;
import io.fire.core.common.io.http.objects.HttpContent;

import io.fire.core.server.FireIoServer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

@AllArgsConstructor
public class IncomingRequest {

    @Getter private HttpContent request;
    @Getter private String url;
    @Getter private ConnectionInfo connectionInfo;
    private Map<String, String> variables;

    public String getVariable(String key) {
        if (!variables.containsKey(key)) return "";
        return variables.get(key);
    }
}
