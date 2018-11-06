package io.fire.core.server.modules.socket.handlers;

import io.fire.core.common.io.enums.ConnectionType;
import io.fire.core.common.io.enums.IoType;
import io.fire.core.common.ratelimiter.RateLimit;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.managers.ClientManager;

import lombok.Getter;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class SelectorHandler implements Runnable {

    private FireIoServer server;
    private ClientManager clientManager;

    @Getter
    private RateLimit rateLimiter = new RateLimit(20, 10);

    //channel selector
    private Selector selector;

    public SelectorHandler(FireIoServer server, Selector selector) {
        this.server = server;
        this.selector = selector;
        //initialize client manager
        this.clientManager = new ClientManager();
    }

    public void setRateLimiter(int timeout, int attempts) {
        rateLimiter.stop();
        rateLimiter = new RateLimit(timeout, attempts);
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

        //check rate limiter for spamming connections
        if (rateLimiter.allowed(socket.getInetAddress().getHostName())) {
            //create and register the connection
            clientManager.references.put(remoteAddr, new SocketClientHandler(server, socket, channel));
            //trigger the on open function in the client handler
            clientManager.references.get(remoteAddr).onOpen();
            //register connection
            channel.register(this.selector, SelectionKey.OP_READ);
        } else {
            //its not allowed
            socket.close();
            channel.close();
        }
    }

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
            if (clientManager.references.get(remoteAddr) != null) {
                clientManager.references.get(remoteAddr).onClose();
                //remove the socket handler from memory, its a dead connection
                clientManager.references.remove(remoteAddr);
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
        if (finalNumRead >= 1001 && (clientManager.references.get(remoteAddr).getIoManager().getIoType() == IoType.WEBSOCKET || clientManager.references.get(remoteAddr).getIoManager().getIoType() == IoType.HTTP || clientManager.references.get(remoteAddr).getIoManager().getIoType() == IoType.UNKNOWN)) {
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
        clientManager.references.get(remoteAddr).getIoManager().handleData(data, server, fufilled);
    }
}
