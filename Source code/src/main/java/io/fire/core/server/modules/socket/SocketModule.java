package io.fire.core.server.modules.socket;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.services.AsyncNetworkService;
import lombok.Getter;

import java.io.IOException;

public class SocketModule {

    @Getter private AsyncNetworkService asyncNetworkService;

    public SocketModule(FireIoServer server, int port) throws IOException {
        //variables
        asyncNetworkService = new AsyncNetworkService(server, port);
    }

}
