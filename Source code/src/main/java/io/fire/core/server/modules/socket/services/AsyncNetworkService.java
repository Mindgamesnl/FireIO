package io.fire.core.server.modules.socket.services;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.client.objects.FireIoConnection;
import io.fire.core.server.modules.socket.handlers.SelectorHandler;
import io.fire.core.server.modules.socket.tasks.IdleKick;
import lombok.Getter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Timer;

public class AsyncNetworkService {

    private FireIoServer server;

    @Getter private SelectorHandler selectorHandler;

    public  AsyncNetworkService(FireIoServer server, int port) throws IOException {
        this.server = server;
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress hostAddress = new InetSocketAddress(port);
        serverChannel.bind(hostAddress);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        selectorHandler = new SelectorHandler(server, selector);

        Thread listenerThread = new Thread(selectorHandler);
        listenerThread.setName("FireioSelectorListener");
        listenerThread.start();

        Timer timer = new Timer();
        timer.schedule(new IdleKick(server), 0, 1000);
    }

    public void broadcast(Packet p) {
        for (FireIoConnection connection : server.getClientModule().connectionMap.values()) {
            try {
                if (connection.getHandler() != null) {
                    connection.getHandler().emit(p);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
