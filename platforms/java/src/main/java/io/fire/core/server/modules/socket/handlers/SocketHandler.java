package io.fire.core.server.modules.socket.handlers;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.SocketServer;
import io.fire.core.server.modules.socket.driver.SocketDriver;
import io.fire.core.server.modules.socket.objects.Connection;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class SocketHandler extends Thread {

    private FireIoServer main;
    private Selector selector;
    private SocketServer socketServer;

    public SocketHandler(FireIoServer main, Selector selector, SocketServer socketServer) {
        this.main = main;
        this.selector = selector;
        this.socketServer = socketServer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int readyCount = selector.select();
                if (readyCount == 0) {
                    continue;
                }

                Set<SelectionKey> readyKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Socket socket = channel.socket();
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();

        socketServer.getOrCreateConnection(socket).onOpen();

        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1001);
        int numRead = -1;
        try {
            numRead = channel.read(buffer);
        } catch (IOException e) {
            //failed to read!
        }

        if (numRead == -1) {
            Socket socket = channel.socket();

            socketServer.getOrCreateConnection(socket).onClose();
            socketServer.removeConnection(socket.getRemoteSocketAddress());

            channel.close();
            key.cancel();
            return;
        }

        int finalNumRead = numRead;

        int fufilled = 0;

        byte[] data = buffer.array();
        fufilled = buffer.flip().limit();

        Connection connection = socketServer.getOrCreateConnection(channel.socket());

        if (finalNumRead >= 1001 && !(connection.getDriver() instanceof SocketDriver)) {
            fufilled = 1001;
            ByteBuffer nextBytes = ByteBuffer.allocate(1001);
            while (channel.read(nextBytes) != 0) {
                byte[] oldData = data;
                int expender = nextBytes.flip().limit();
                fufilled += expender;
                byte[] temp = new byte[oldData.length + expender];
                System.arraycopy(oldData, 0, temp,0 , oldData.length);
                System.arraycopy(nextBytes.array(), 1, temp, oldData.length, expender - 1);
                data = temp;
                if (expender >= 1001) nextBytes = ByteBuffer.allocate(1001);
            }
        }

        try {
            connection.onData(data, fufilled);
        } catch (Exception e) {
            System.err.println("[Fire-IO] Failed to handle a packet.");
            e.printStackTrace();
        }
    }
}