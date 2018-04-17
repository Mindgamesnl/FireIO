package io.fire.core.server.modules.socket.handlers;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.SerialReader;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.managers.ClientManager;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class SelectorHandler extends SerialReader implements Runnable {

    private FireIoServer server;
    @Getter @Setter private boolean updatedBuffer = false;
    @Getter @Setter private Integer byteArrayLength = 1024;
    private Selector selector;
    private ClientManager clientManager;

    public SelectorHandler(FireIoServer server, Selector selector) {
        this.server = server;
        this.selector = selector;
        this.clientManager = new ClientManager();
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
                Iterator iterator = readyKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();

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
        clientManager.references.put(remoteAddr, new SocketClientHandler(server, socket, channel));
        clientManager.references.get(remoteAddr).onOpen();
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(byteArrayLength);
        int numRead = -1;
        try {
            numRead = channel.read(buffer);
        } catch (IOException e) {
            //failed to read!
        }

        if (numRead == -1) {
            Socket socket = channel.socket();
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            clientManager.references.get(remoteAddr).onClose();
            clientManager.references.remove(remoteAddr);
            channel.close();
            key.cancel();
            return;
        }

        byte[] data = new byte[numRead];

        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        SocketAddress remoteAddr = channel.socket().getRemoteSocketAddress();
        Packet[] packets = fromString(new String(data));
        for (Packet p : packets) {
            clientManager.references.get(remoteAddr).onPacket(p);
        }
    }
}
