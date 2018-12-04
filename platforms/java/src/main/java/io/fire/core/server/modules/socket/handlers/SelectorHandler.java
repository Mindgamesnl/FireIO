package io.fire.core.server.modules.socket.handlers;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.enums.ConnectionType;
import io.fire.core.common.ratelimiter.RateLimit;
import io.fire.core.server.FireIoServer;

import lombok.Getter;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SelectorHandler implements Runnable {

    private FireIoServer server;
    @Getter public Map<SocketAddress, SocketClientHandler> references = new ConcurrentHashMap<>();

    @Getter
    private RateLimit rateLimiter = new RateLimit(20, 10);

    //channel selector
    private Selector selector;


    /**
     * Setup a selector handler, this so the socket can read and receive data at any time
     *
     * @param server
     * @param selector
     */
    public SelectorHandler(FireIoServer server, Selector selector) {
        this.server = server;
        this.selector = selector;
    }


    /**
     * set or change the rate limiter settings
     * this overwrites the current or default settings
     *
     * @param timeout
     * @param attempts
     */
    public void setRateLimiter(int timeout, int attempts) {
        rateLimiter.stop();
        rateLimiter = new RateLimit(timeout, attempts);
    }


    /**
     * Main loop
     *
     * this keeps on checking the socket in a new thread
     * for new socket channel messages, when found it gets tossed over to the client handler.
     *
     * If a new connection is detected, we check it for authentication and register it on our side,
     * and finish up the handshake if all seems to be legit.
     */
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


    /**
     * A new client tried to open a connection, check it for validation and register it
     *
     * @param key
     * @throws IOException
     */
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

        //check rate limiter for spamming connections
        if (rateLimiter.allowed(socket.getInetAddress().getHostName())) {
            //create and register the connection
            references.put(remoteAddr, new SocketClientHandler<Packet>(server, socket, channel));
            //trigger the on open function in the client handler
            references.get(remoteAddr).onOpen();
            //register connection
            channel.register(this.selector, SelectionKey.OP_READ);
        } else {
            //its not allowed
            references.remove(channel.socket().getRemoteSocketAddress());
            socket.close();
            if (channel != null) {
                channel.close();
            }
        }
    }


    /**
     * A client send data to the server.
     *
     * Parse it with the full size (follows the Fire-IO protocol spec but also accepts some other protocols)
     * @param key
     * @throws IOException
     */
    private void read(SelectionKey key) throws IOException {
        //get socket channel that has send the data
        SocketChannel channel = (SocketChannel) key.channel();
        //create buffer with the common buffer size
        //set default to -1 (for when shit goes wrong)
        ByteBuffer buffer = ByteBuffer.allocate(1001);
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
            if (references.get(remoteAddr) != null) {
                references.get(remoteAddr).onClose();
                //remove the socket handler from memory, its a dead connection
                references.remove(remoteAddr);
            }
            //close channel
            channel.close();
            //close key
            key.cancel();
            //stop
            return;
        }

        int finalNumRead = numRead;

        int fufilled = 0;

        //get adress
        SocketAddress remoteAddr = channel.socket().getRemoteSocketAddress();

        //check if there may be more
        //read the byte data
        byte[] data = buffer.array();
        fufilled = buffer.flip().limit();

        //check if we may need to check for more data
        if (finalNumRead >= 1001 && (references.get(remoteAddr).getIoManager().getIoType() == ConnectionType.WEBSOCKET || references.get(remoteAddr).getIoManager().getIoType() == ConnectionType.HTTP || references.get(remoteAddr).getIoManager().getIoType() == ConnectionType.NONE)) {
            fufilled = 1001;
            ByteBuffer nextBytes = ByteBuffer.allocate(1001);
            while (channel.read(nextBytes) != 0) {
                byte[] oldData = data;
                int expender = nextBytes.flip().limit();
                fufilled += expender;
                byte[] temp = new byte[oldData.length + expender];
                System.arraycopy(oldData, 0, temp,0 , oldData.length);
                //append newly received packet content
                System.arraycopy(nextBytes.array(), 1, temp, oldData.length, expender - 1);
                data = temp;
                if (expender >= 1001) nextBytes = ByteBuffer.allocate(1001);
            }
        }

        //parse all packets
        try {
            references.get(remoteAddr).getIoManager().handleData(data, server, fufilled);
        } catch (Exception e) {
            System.err.println("[Fire-IO] Failed to handle a packet.");
            e.printStackTrace();
        }
    }
}
