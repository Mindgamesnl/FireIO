package io.fire.core.server.modules.socket;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.enums.BlockedProtocol;
import io.fire.core.server.modules.socket.services.AsyncNetworkService;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SocketModule {

    @Getter private AsyncNetworkService asyncNetworkService;
    @Getter private List<BlockedProtocol> blockedProtocolList = new ArrayList<>();


    /**
     * Setup of the network service
     *
     * @param server
     * @param port
     * @throws IOException
     */
    public SocketModule(FireIoServer server, int port) throws IOException {
        //variables
        asyncNetworkService = new AsyncNetworkService(server, port);
    }

}
