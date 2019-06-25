package io.fire.core.server;

import io.fire.core.common.events.EventHandler;
import io.fire.core.server.modules.http.HttpProvider;
import io.fire.core.server.modules.socket.SocketServer;
import lombok.Getter;

import java.io.IOException;

public class FireIoServer {

    //modules
    @Getter private int port;
    @Getter private HttpProvider httpProvider;
    @Getter private SocketServer socketServer;
    @Getter private EventHandler eventHandler = new EventHandler();

    public FireIoServer(int port) throws IOException {
        this.port = port;
        this.httpProvider = new HttpProvider(this);
        this.socketServer = new SocketServer(this, port);
        System.out.println("[Fire-IO] Attaching to port " + port);
    }

    public FireIoServer setPassword(String password) {
        this.socketServer.setPassword(password);
        return this;
    }
}
