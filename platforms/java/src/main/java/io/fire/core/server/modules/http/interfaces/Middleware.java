package io.fire.core.server.modules.http.interfaces;

import io.fire.core.server.modules.http.objects.MiddlewareHandler;
import io.fire.core.server.modules.http.objects.Request;
import io.fire.core.server.modules.http.objects.Response;

public interface Middleware {

    void onRequest(Request request, Response response, MiddlewareHandler handler);

}
