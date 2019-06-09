package io.fire.core.server.modules.socket;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.handlers.SocketHandler;
import io.fire.core.server.modules.socket.objects.Connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;

public class SocketServer {

    private Map<String, Connection> connectionMap = new HashMap<>();

    private FireIoServer main;

    public SocketServer(FireIoServer main, int port) throws IOException {
        this.main = main;

        // register and start the socket server
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        InetSocketAddress hostAddress = new InetSocketAddress(port);
        serverChannel.bind(hostAddress);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        Thread executorThread = new SocketHandler(selector, this);
        executorThread.start();
    }

    public Connection getOrCreateConnection(Socket socket) {
        Connection storedConnection = connectionMap.get(socket.getRemoteSocketAddress().toString());
        if (storedConnection == null) {
            storedConnection = new Connection(socket, main);
            connectionMap.put(socket.getRemoteSocketAddress().toString(), storedConnection);
        }
        return storedConnection;
    }

    public void removeConnection(SocketAddress host) {
        System.out.println("remove");
        connectionMap.remove(host.toString());
    }

}
