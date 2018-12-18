package io.fire.core.server.modules.socket;

import io.fire.core.common.ratelimiter.RateLimit;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.FireIoConnection;
import io.fire.core.server.modules.socket.enums.BlockedProtocol;
import io.fire.core.server.modules.socket.handlers.SelectorHandler;
import io.fire.core.server.modules.socket.handlers.SocketClientHandler;
import io.fire.core.server.modules.socket.services.AsyncNetworkService;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SocketModule {

    @Getter private AsyncNetworkService asyncNetworkService;
    @Getter private List<BlockedProtocol> blockedProtocolList = new ArrayList<>();
    @Getter public Map<SocketAddress, SocketClientHandler> ipMap = new ConcurrentHashMap<>();
    @Setter @Getter private RateLimit rateLimiter = new RateLimit(20, 10);

    private Map<UUID, SelectorHandler> socketWorkers = new ConcurrentHashMap<>();

    private FireIoServer fireIoServer;


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
        this.fireIoServer = server;
    }

    public void scale(int target) {
        if (target == 0 || target <= (socketWorkers.size())) throw new IllegalArgumentException("There must be at least 1 active socket working.");
    }

    public void suspend(SelectorHandler susspendedHandler) {
        //remove from map
        socketWorkers.remove(susspendedHandler.getId());

        //enable all clients to switch from provider
        for (FireIoConnection fireIoConnection : fireIoServer.getClientModule().getAll())
            if (fireIoConnection.getHandler().getIoManager().getInteractionHandler() == susspendedHandler.getId())
                fireIoConnection.getHandler().getIoManager().setInteractionHandler(null);
    }

}
