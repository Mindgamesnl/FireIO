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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SocketModule {

    @Getter private List<BlockedProtocol> blockedProtocolList = new ArrayList<>();
    @Getter public Map<SocketAddress, SocketClientHandler> ipMap = new ConcurrentHashMap<>();
    @Setter @Getter private RateLimit rateLimiter = new RateLimit(20, 10);

    private Map<UUID, SelectorHandler> socketWorkers = new ConcurrentHashMap<>();

    private Selector selector;
    private ServerSocketChannel serverChannel;

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
        this.fireIoServer = server;

        //start server
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress hostAddress = new InetSocketAddress(port);
        serverChannel.bind(hostAddress);
        selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        //set default
        setHandlerAmount(1);
    }

    public void setHandlerAmount(int target) throws IOException {
        if (target <= 0) throw new IllegalArgumentException("There must be at least 1 active socket working.");
        System.out.println("[Fire-IO] Setting socket handler amount to " + target);

        //remove all old
        socketWorkers.values().forEach(this::suspend);
        for (int i=1; i<=target; i++) addHandler();
    }

    private void addHandler() throws IOException {
        SelectorHandler selectorHandler = new SelectorHandler(fireIoServer, selector);
        socketWorkers.put(selectorHandler.getId(), selectorHandler);
        new Thread(selectorHandler).start();
    }

    public void suspend(SelectorHandler susspendedHandler) {
        //remove from map
        socketWorkers.remove(susspendedHandler.getId());

        //enable all clients to switch from provider
        for (FireIoConnection fireIoConnection : fireIoServer.getClientModule().getAll())
            if (fireIoConnection.getHandler().getIoManager().getInteractionHandler() == susspendedHandler.getId())
                fireIoConnection.getHandler().getIoManager().setInteractionHandler(null);
    }


    /**
     * set or change the rate limiter settings
     * this overwrites the current or default settings
     *
     * @param timeout
     * @param attempts
     */
    public void setRateLimiter(int timeout, int attempts) {
        fireIoServer.getSocketModule().getRateLimiter().stop();
        setRateLimiter(new RateLimit(timeout, attempts));
    }

}
