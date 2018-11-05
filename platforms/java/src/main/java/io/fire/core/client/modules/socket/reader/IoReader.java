package io.fire.core.client.modules.socket.reader;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.handlers.AsyncConnectionHandler;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.packets.ReceivedText;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoReader implements Runnable {

    //channel and client
    private SocketChannel channel;
    private FireIoClient client;

    //connection handler
    private AsyncConnectionHandler asyncConnectionHandler;

    public IoReader(SocketChannel channel, AsyncConnectionHandler ins, FireIoClient client) {
        this.channel = channel;
        this.client = client;
        this.asyncConnectionHandler = ins;
    }

    @Override
    public void run() {
        //infinite while loop in its own thread to continually check for new packets
        while (true) {
            try {
                ByteBuffer buffer = ByteBuffer.allocate(1001);
                int numRead = -1;
                try {
                    //read buffer
                    numRead = channel.read(buffer);
                } catch (IOException e) {
                    //could not read from channel! (timed out)
                    client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Connection timed out! (server unreachable)", null));
                    //failed to read!
                }

                if (numRead == -1) {
                    //data is -1
                    //this means that the other end of the socket closed the connection
                    //handle it and trigger the event
                    asyncConnectionHandler.onClose();
                    client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Connection timed out! (invalid data)", null));
                    //close channel, its dead anyhow
                    channel.close();
                    //exit, don't try to parse it anyway
                    return;
                }

                int finalNumRead = numRead;
                //read and parse byte array
                byte[] data = new byte[finalNumRead];
                System.arraycopy(buffer.array(), 0, data, 0, finalNumRead);

                //parse them to packets!
                //in semi-rare cases the system stitches multiple packets in one stream to save on load
                //this can mean that we receive multiple packets in one go!
                asyncConnectionHandler.getIoManager().handleData(data, client, finalNumRead);
            } catch (Exception e) {
                //invalid buffer! oh no...
                client.getEventHandler().fireEvent(Event.CLOSED_UNEXPECTEDLY, new ReceivedText("Invalid buffer! (" + e.getMessage() + ")", null));
                e.printStackTrace();
            }

        }
    }
}