package io.fire.core.common.io.api.api;

import io.fire.core.common.io.http.objects.ConnectionInfo;
import io.fire.core.common.io.http.objects.HttpContent;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
public class IncomingRequest {

    @Getter private HttpContent request;
    @Getter private String url;
    @Getter private ConnectionInfo connectionInfo;
    private Map<String, String> variables;

    /**
     * Get a url variable, return an empty string if it does not exist to prevent NPE'S in the API
     *
     * @param key
     * @return
     */
    public String getVariable(String key) {
        if (!variables.containsKey(key)) return "";
        return variables.get(key);
    }
}
