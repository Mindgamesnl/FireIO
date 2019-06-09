package io.fire.core.server.modules.socket.driver;

import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.common.io.http.objects.HttpContent;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.http.interfaces.Middleware;
import io.fire.core.server.modules.http.objects.*;
import io.fire.core.server.modules.http.routes.FileStatusRoute;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.server.modules.socket.objects.Connection;

import java.io.IOException;
import java.net.Socket;

public class HttpDriver implements NetworkDriver {

    private Socket socket;
    private FireIoServer main;
    private Connection connection;

    HttpDriver(Socket socket, FireIoServer main, Connection connection) {
        this.socket = socket;
        this.main = main;
        this.connection = connection;
    }

    @Override
    public void onError() {

    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {

    }

    @Override
    public void onData(byte[] data, Integer length) {
        try {
            HttpContent httpContent = new HttpContent(new String(data));
            ExecutableRoute route = main.getHttpProvider().getRoute(httpContent.getUrl());

            Response response = new Response(byteBuffer -> {
                try {
                    main.getSocketServer().removeConnection(socket.getRemoteSocketAddress());
                    socket.getChannel().write(byteBuffer);
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            Request request = new Request(httpContent, route.getVariables(), socket.getInetAddress());
            MiddlewareHandler middlewareHandler = new MiddlewareHandler(route.getRoute(), connection);

            //handle all middleware
            for (Middleware middleware : main.getHttpProvider().getMiddleware().values()) {
                middleware.onRequest(request, response, middlewareHandler);
            }

            if (!middlewareHandler.getCancelled()) route.getRoute().getRouteHandler().onRequest(request, response);
        } catch (Exception e) {
            ExecutableRoute errorRoute = new ExecutableRoute(null, new Route(null, new FileStatusRoute(HttpStatusCode.C_500, "500.html").setReplacement("{error}", e.getClass().getName())));
            errorRoute.getRoute().getRouteHandler().onRequest(null, new Response(byteBuffer -> {
                try {
                    socket.getChannel().write(byteBuffer);
                    socket.close();
                    main.getSocketServer().removeConnection(socket.getRemoteSocketAddress());
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }));
            e.printStackTrace();
        }
    }
}
