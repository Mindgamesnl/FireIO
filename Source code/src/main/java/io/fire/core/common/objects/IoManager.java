package io.fire.core.common.objects;

import io.fire.core.common.interfaces.Packet;
import lombok.Setter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class IoManager {

    //socket
    private SocketChannel channel;

    //lock
    private ReentrantLock lock = new ReentrantLock();

    //list
    private ConcurrentLinkedDeque<Packet> waiting = new ConcurrentLinkedDeque<>();

    //buffer
    private Queue<Byte> byteBuffer = new ConcurrentLinkedDeque<Byte>() {};
    private Byte[] portion = new Byte[4];
    private int portionIndex = 0;
    private boolean isNegative = false;

    @Setter
    private Consumer<Packet> onInput = (p) -> {};

    public IoManager(SocketChannel channel) {
        this.channel = channel;
    }

    public void handleData(byte[] input) {
        for (byte a : input) {
            switch (((char) a)) {
                case 's':
                    spliceBuffer();
                    byte[] ret = new byte[byteBuffer.toArray().length];
                    Iterator<Byte> iterator = byteBuffer.iterator();
                    for (int i = 0; i < ret.length; i++) ret[i] = iterator.next();
                    byteBuffer.clear();

                    ByteArrayInputStream bis = new ByteArrayInputStream(ret);
                    ObjectInput in = null;
                    Packet finalOut = null;
                    try {
                        in = new ObjectInputStream(bis);
                        finalOut = (Packet) in.readObject();

                    } catch (IOException e) {
                        System.err.println("UNABLE TO DECODE PACKET!!!");
                        System.err.println("Error: ");
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        System.err.println("UNABLE TO DECODE PACKET!!!");
                        System.err.println("Error: ");
                        e.printStackTrace();
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException ex) {
                            // ignore close exception
                        }
                    }

                    onInput.accept(finalOut);
                    break;
                case ',':
                    spliceBuffer();
                    break;
                case '-':
                    isNegative = true;
                    break;
                default:
                    portion[portionIndex] = Byte.decode((char) a + "");
                    portionIndex++;
                    break;
            }
        }
    }

    private void spliceBuffer() {
        StringBuilder bytePart = new StringBuilder();
        if (isNegative) bytePart.append("-");
        for (Byte part : portion) if (part != null) bytePart.append(part);
        byteBuffer.add(Byte.decode(bytePart.toString()));
        portion = new Byte[4];
        portionIndex = 0;
        isNegative = false;
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
            StringBuilder out = new StringBuilder();
            int looped = 0;
            byte[] a = prepared.array();
            for (byte b : a) {
                looped++;
                if (looped == a.length) {
                    out.append(b);
                } else {
                    out.append(b).append(",");
                }
            }
            prepared = ByteBuffer.wrap((out + "s").getBytes());
            write(prepared);
            waiting.remove(packet);
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

    //makes it ex, but don't forget to close the buffer!
    private ByteBuffer prepare(Packet packet) {
        //create buffer and output streams
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outBytes = new byte[0];
        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(packet);
            out.flush();
            outBytes = bos.toByteArray();
        } catch (IOException e) {
            System.err.println("UNABLE TO DECODE PACKET!!!");
            System.err.println("Error: ");
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        ByteBuffer buffer = ByteBuffer.allocate(outBytes.length);
        buffer.put(outBytes);
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
