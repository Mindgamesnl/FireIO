package io.fire.core.common.objects;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.SocketEvents;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;

public class IoManager {

    //socket
    private SocketChannel channel;
    private SocketEvents handler;
    private PacketHelper packetHelper;

    //lock
    private ReentrantLock lock = new ReentrantLock();

    //list
    private ConcurrentLinkedDeque<Packet> waiting = new ConcurrentLinkedDeque<>();

    public IoManager(SocketChannel channel, SocketEvents handler, PacketHelper packetHelper) {
        this.channel = channel;
        this.handler = handler;
        this.packetHelper = packetHelper;
    }

    public void send(Packet packet) throws IOException {
        if (lock.isLocked()) {
            //add to list to handle later! socket is busy
            waiting.add(packet);
        } else {
            //lock
            this.lock.lock();
            //serialize
            ByteBuffer prepared = prepare(packet);
            String out = "";
            int looped = 0;
            byte[] a = prepared.array();
            for (byte b : a) {
                looped++;
                if (looped == a.length) {
                    out += b;
                } else {
                    out += b + ",";
                }
            }
            prepared = ByteBuffer.wrap((out + "s").getBytes());
            write(prepared);
            if (waiting.contains(packet)) waiting.remove(packet);
            lock.unlock();
        }
    }

    public void flushWaiting() {
        waiting.forEach(w -> {
            try {
                send(w);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    //makes it ex, but dont forget to close the buffer!
    private ByteBuffer prepare(Packet packet) {
        byte[] out = this.packetHelper.toString(packet);
        ByteBuffer buffer = ByteBuffer.allocate(out.length);
        buffer.put(out);
        buffer.flip();
        return buffer;
    }

    private void write(ByteBuffer buffer) throws IOException {
        write(new ByteBuffer[]{buffer});
    }

    private void write(ByteBuffer[] buffers) throws IOException {
        for (ByteBuffer buffer : buffers) {
            this.channel.write(buffer);
            buffer.clear();
        }
    }

}
