package io.fire.core.client.modules.socket.reader;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.handlers.AsyncConnectionHandler;
import io.fire.core.common.eventmanager.enums.Event;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class IoReader implements Runnable {

    //channel and client
    private SocketChannel channel;
    private FireIoClient client;

    //connection handler
    private AsyncConnectionHandler asyncConnectionHandler;

    /**
     * Setup a new IO reader
     *
     * @param channel
     * @param ins
     * @param client
     */
    public IoReader(SocketChannel channel, AsyncConnectionHandler ins, FireIoClient client) {
        this.channel = channel;
        this.client = client;
        this.asyncConnectionHandler = ins;
    }


    /**
     * Function by interface, wait for data send when appropriate.
     */
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
                    client.getEventHandler().triggerEvent(Event.TIMED_OUT, null, "Connection timed out! (server unreachable)");
                    //failed to read!
                }

                if (numRead == -1) {
                    //data is -1
                    //this means that the other end of the socket closed the connection
                    //handle it and trigger the event
                    asyncConnectionHandler.onClose();
                    client.getEventHandler().triggerEvent(Event.TIMED_OUT, null, "Connection timed out! (invalid data)");
                    //close channel, its dead anyhow
                    channel.close();
                    //exit, don't try to parse it anyway
                    return;
                }

                int finalNumRead = numRead;

                int fufilled = 0;

                //check if there may be more
                //read the byte data
                byte[] data = buffer.array();
                fufilled = ((Buffer)buffer).flip().limit();

                //check if we may need to check for more data
                if (finalNumRead >= 1001) {
                    fufilled = 1001;
                    ByteBuffer nextBytes = ByteBuffer.allocate(1001);
                    while (channel.read(nextBytes) != 0) {
                        byte[] oldData = data;
                        int expender = ((Buffer)nextBytes).flip().limit();
                        fufilled += expender;
                        byte[] temp = new byte[oldData.length + expender];
                        System.arraycopy(oldData, 0, temp,0 , oldData.length);
                        //append newly received packet content
                        System.arraycopy(nextBytes.array(), 1, temp, oldData.length, expender - 1);
                        data = temp;
                        if (expender >= 1001) nextBytes = ByteBuffer.allocate(1001);
                    }
                }

                //parse them to packets!
                //in semi-rare cases the system stitches multiple packets in one stream to save on load
                //this can mean that we receive multiple packets in one go!
                asyncConnectionHandler.getIoManager().handleData(data, client, fufilled);
            } catch (Exception e) {
                //invalid buffer! oh no...
                client.getEventHandler().triggerEvent(Event.TIMED_OUT, null, "Invalid buffer! (" + e.getMessage() + ")");
                e.printStackTrace();
            }

        }
    }
}