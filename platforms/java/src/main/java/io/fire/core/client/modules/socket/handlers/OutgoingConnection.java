package io.fire.core.client.modules.socket.handlers;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.events.enums.Event;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class OutgoingConnection implements Runnable {

    private FireIoClient client;

    private Boolean isConnected;
    private SocketChannel socketChannel;
    private Thread socketListener;

    public OutgoingConnection(FireIoClient client) throws IOException {
        this.client = client;
        socketChannel = SocketChannel.open(new InetSocketAddress(client.getHost(), client.getPort()));
        socketChannel.configureBlocking(true);
        socketListener = new Thread(this);
        socketListener.start();
        isConnected = true;
    }

    public void shutdown(Event event, String cause) {
        isConnected = false;
        try {
            socketChannel.close();
        } catch (IOException e) {
            //ignored
        }
        socketListener.stop();
        client.getEventHandler().triggerEvent(event, null, cause);
    }

    @Override
    public void run() {
        while (true) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1001);
                int numRead = -1;
                try {
                    numRead = socketChannel.read(buffer);
                } catch (IOException e) {
                    shutdown(Event.TIMED_OUT, "Connection timed out! (server unreachable)");
                }

                if (numRead == -1) {
                    shutdown(Event.TIMED_OUT, "Connection timed out! (invalid data)");
                    channel.close();
                    return;
                }

                int finalNumRead = numRead;
                byte[] data = new byte[finalNumRead];
                System.arraycopy(buffer.array(), 0, data, 0, finalNumRead);

                asyncConnectionHandler.getIoManager().handleData(data, client, finalNumRead);
            } catch (Exception e) {
                shutdown(Event.TIMED_OUT, "Invalid buffer! (" + e.getMessage() + ")");
                e.printStackTrace();
            }
        }
    }
}
