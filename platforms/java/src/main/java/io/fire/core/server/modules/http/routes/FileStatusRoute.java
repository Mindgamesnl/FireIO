package io.fire.core.server.modules.http.routes;

import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.server.modules.http.interfaces.RouteHandler;
import io.fire.core.server.modules.http.objects.Request;
import io.fire.core.server.modules.http.objects.Response;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FileStatusRoute implements RouteHandler {

    private Map<String, String> replacements = new HashMap<>();
    private HttpStatusCode statusCode;
    private String file;

    public FileStatusRoute(HttpStatusCode statusCode, String file) {
        this.statusCode = statusCode;
        this.file = file;
    }

    public FileStatusRoute setReplacement(String key, String value) {
        this.replacements.put(key, value);
        return this;
    }

    @Override
    public void onRequest(Request request, Response response) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("html/" + file);
        assert is != null;
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String out  = s.hasNext() ? s.next() : "";

        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            out = out.replace(key, value);
        }

        response.setResponseType(HttpContentType.HTML);
        response.setStatus(statusCode);
        response.send(out);
    }
}
