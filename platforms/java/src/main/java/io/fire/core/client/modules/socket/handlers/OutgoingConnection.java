package io.fire.core.client.modules.socket.handlers;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.drivers.ClientDriver;
import io.fire.core.common.events.enums.Event;
import lombok.Getter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class OutgoingConnection implements Runnable {

    private FireIoClient client;

    private Boolean isConnected;
    private SocketChannel socketChannel;
    @Getter private ClientDriver clientDriver;
    private Thread socketListener;
    private Selector selector;

    public OutgoingConnection(FireIoClient client) throws IOException {
        this.client = client;
        socketChannel = SocketChannel.open(new InetSocketAddress(client.getHost().getHost(), client.getHost().getPort()));
        socketChannel.configureBlocking(false);
        selector = Selector.open();
        socketChannel.register(this.selector, SelectionKey.OP_READ);
        socketListener = new Thread(this);
        socketListener.start();
        isConnected = true;

        this.clientDriver = new ClientDriver(client, socketChannel.socket());
        this.clientDriver.onOpen();
    }

    public void shutdown(Event event, String cause) {
        isConnected = false;
        try {
            socketChannel.close();
        } catch (IOException e) {
            //ignored
        }
        socketListener.stop();
        client.getEventHandler().triggerEvent(event, cause);
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

                    if (key.isReadable()) {
                        read(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
            this.clientDriver.onError();
            this.clientDriver.onClose();

            channel.close();
            key.cancel();
            return;
        }

        byte[] data = buffer.array();
        int fufilled = 1001;
        ByteBuffer nextBytes = ByteBuffer.allocate(1001);
        while (channel.read(nextBytes) != 0) {
            byte[] oldData = data;
            int expender = nextBytes.limit();
            fufilled += expender;
            byte[] temp = new byte[oldData.length + expender];
            System.arraycopy(oldData, 0, temp, 0, oldData.length);
            System.arraycopy(nextBytes.array(), 1, temp, oldData.length, expender - 1);
            data = temp;
            if (expender >= 1001) nextBytes = ByteBuffer.allocate(1001);
        }

        try {
            this.clientDriver.onData(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
