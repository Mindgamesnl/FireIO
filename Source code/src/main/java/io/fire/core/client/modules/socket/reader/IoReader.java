package io.fire.core.client.modules.socket.reader;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.handlers.AsyncConnectionHandler;
import io.fire.core.common.events.enums.Event;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.SerialReader;
import io.fire.core.common.packets.ReceivedText;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class IoReader extends SerialReader implements Runnable {

    @Getter @Setter private int bufferSize = 1024;
    private SocketChannel channel;
    private FireIoClient client;
    private AsyncConnectionHandler asyncConnectionHandler;

    public IoReader(SocketChannel channel, int buffer, AsyncConnectionHandler ins, FireIoClient client) {
        this.bufferSize = buffer;
        this.channel = channel;
        this.client = client;
        this.asyncConnectionHandler = ins;
    }

    @Override
    public void run() {
        while (true) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
                int numRead = -1;
                try {
                    numRead = channel.read(buffer);
                } catch (IOException e) {
                    client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Connection timed out! (server unreachable)" ,null));
                    //failed to read!
                }

                if (numRead == -1) {
                    Socket socket = channel.socket();
                    SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                    asyncConnectionHandler.onClose();
                    client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Connection timed out! (invalid data)" ,null));
                    channel.close();
                    return;
                }

                byte[] data = new byte[numRead];
                System.arraycopy(buffer.array(), 0, data, 0, numRead);
                SocketAddress remoteAddr = channel.socket().getRemoteSocketAddress();
                Packet[] packets = fromString(new String(data));
                for (Packet p : packets) {
                    asyncConnectionHandler.onPacket(p);
                }
            } catch (Exception e) {
                client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Invalid buffer! (" + e.getMessage() + ")" ,null));
            }

        }
    }
}