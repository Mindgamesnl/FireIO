package io.fire.core.common.io.api.api;

import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
public class ResponseSettings {

    @Setter @Getter private HttpContentType content = HttpContentType.TEXT;
    @Setter @Getter private HttpStatusCode statusCode = HttpStatusCode.C_200;
    private Map<String, String> headers = new HashMap<>();

    public void setHeader(String headerKey, String headerValue) {
        headers.put(headerKey, headerValue);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

}
