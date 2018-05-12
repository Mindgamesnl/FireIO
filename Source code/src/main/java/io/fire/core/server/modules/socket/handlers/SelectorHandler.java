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
    private ClientManager clientManager;

    //the default buffer is common in everything of Fire-Io, when bigger data is getting send it will change in the whole network to what ever is needed.
    //The default is 5KB
    @Getter @Setter private Integer byteArrayLength = 5120;
    @Getter @Setter private boolean updatedBuffer = false;

    //channel selector
    private Selector selector;

    public SelectorHandler(FireIoServer server, Selector selector) {
        this.server = server;
        this.selector = selector;
        //initialize client manager
        this.clientManager = new ClientManager();
    }

    @Override
    public void run() {
        //loop for ever checking and accepting new incoming streams
        while (true) {
            try {
                //check how many packets there are left to read
                int readyCount = selector.select();
                //are there none?
                if (readyCount == 0) {
                    //then continue
                    continue;
                }

                //get all keys
                Set<SelectionKey> readyKeys = selector.selectedKeys();
                //crate an iterator for incoming keys
                Iterator iterator = readyKeys.iterator();
                //loop
                while (iterator.hasNext()) {
                    //cast a copy of a selection key
                    SelectionKey key = (SelectionKey) iterator.next();
                    //remove it from the iterator
                    iterator.remove();
                    //check if the key is valid, if not, skip it
                    if (!key.isValid()) {
                        continue;
                    }
                    //is it a new connection that needs to be accepted?
                    if (key.isAcceptable()) {
                        //accept it
                        accept(key);
                    } else if (key.isReadable()) {
                        //read it out
                        read(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void accept(SelectionKey key) throws IOException {
        //accept a new connection
        //get the channel
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        //accept the channel
        SocketChannel channel = serverChannel.accept();
        //non blocking, handle it async
        channel.configureBlocking(false);
        //get the socket
        Socket socket = channel.socket();
        //get the address, used to identify it at first before it has a key
        SocketAddress remoteAddr = socket.getRemoteSocketAddress();
        //create and register the connection
        clientManager.references.put(remoteAddr, new SocketClientHandler(server, socket, channel));
        //trigger the on open function in the client handler
        clientManager.references.get(remoteAddr).onOpen();
        //register connection
        channel.register(this.selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) throws IOException {
        //get socket channel that has send the data
        SocketChannel channel = (SocketChannel) key.channel();
        //create buffer with the common buffer size
        ByteBuffer buffer = ByteBuffer.allocate(byteArrayLength);
        //set default to -1 (for when shit goes wrong)
        int numRead = -1;
        try {
            //read from channel with buffer
            numRead = channel.read(buffer);
        } catch (IOException e) {
            //failed to read!
        }

        //did it fail to read?
        if (numRead == -1) {
            //it failed to read..
            //get the connection that failed
            Socket socket = channel.socket();
            //get the adress
            SocketAddress remoteAddr = socket.getRemoteSocketAddress();
            //handle the close handler in the socket handler
            clientManager.references.get(remoteAddr).onClose();
            //remove the socket handler from memory, its a dead connection
            clientManager.references.remove(remoteAddr);
            //close channel
            channel.close();
            //close key
            key.cancel();
            //stop
            return;
        }

        //read the byte data
        byte[] data = new byte[numRead];

        //copy array with parameters
        System.arraycopy(buffer.array(), 0, data, 0, numRead);
        //get adress
        SocketAddress remoteAddr = channel.socket().getRemoteSocketAddress();
        //parse all packets
        Packet[] packets = fromString(new String(data));
        //some times, packets get stitched together when they are send in quick completion of one another to save on resources
        for (Packet p : packets) {
            //get the client and trigger the packet handler
            clientManager.references.get(remoteAddr).onPacket(p);
        }
    }
}
