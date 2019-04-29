package io.fire.core.server.modules.http.interfaces;

import io.fire.core.server.modules.http.objects.Request;
import io.fire.core.server.modules.http.objects.Response;

public interface RouteHandler {

    void onRequest(Request request, Response response);

}
