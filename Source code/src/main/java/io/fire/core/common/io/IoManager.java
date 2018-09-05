package io.fire.core.common.io;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.PoolHolder;
import lombok.Setter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
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

    //buffer for the fireio protocol
    private Queue<Byte> byteBuffer = new ConcurrentLinkedDeque<Byte>() {};
    private Byte[] portion = new Byte[4];
    private int portionIndex = 0;
    private boolean isNegative = false;

    //buffers for the websocket protocol
    private List<String> dataLines = new ArrayList<>();
    private String wsString = "";

    //protocol type
    private Boolean hasReceived = false;
    private IoType ioType = IoType.UNKNOWN;

    @Setter
    private Consumer<Packet> packetHandler = (p) -> {};

    public IoManager(SocketChannel channel) {
        this.channel = channel;
    }

    public void handleData(byte[] input, PoolHolder poolHolder) {

        if (!hasReceived) {
            byte first = input[0];
            if (((char) first) == 'G') {
                //the first is the G from GET
                this.ioType = IoType.WEBSOCKET;
            } else {
                this.ioType = IoType.FIREIO;
            }
            hasReceived = true;
        }

        switch (this.ioType) {
            case FIREIO:
                //use FireIo driver
                for (byte a : input) {
                    switch (((char) a)) {
                        case 's':
                            spliceBuffer();
                            byte[] ret = new byte[byteBuffer.toArray().length];
                            Iterator<Byte> iterator = byteBuffer.iterator();
                            for (int i = 0; i < ret.length; i++) ret[i] = iterator.next();
                            byteBuffer.clear();
                            ByteArrayInputStream bis = new ByteArrayInputStream(ret);
                            Packet finalOut = null;
                            try (ObjectInput in = new ObjectInputStream(bis)) {
                                finalOut = (Packet) in.readObject();
                            } catch (IOException | ClassNotFoundException e) {
                                System.err.println("UNABLE TO DECODE PACKET!!!");
                                System.err.println("Error: ");
                                e.printStackTrace();
                            }
                            Packet finalOut1 = finalOut;
                            poolHolder.getPool().run(()-> packetHandler.accept(finalOut1));
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
                break;

            case WEBSOCKET:
                //use Websocket driver
                for (byte a : input) {
                    System.out.print(((char) a));
                    if (((char) a) == '\n') {
                        dataLines.add(wsString);
                        wsString = "";
                        if (dataLines.size() != 0 && dataLines.get(dataLines.size() - 1).length() == 1) {
                            dataLines.remove(dataLines.size() - 1);
                            System.out.println("ended line! all lines:");
                            dataLines.forEach(s -> System.out.println(" - " + s.length() + " - " + s));
                        }

                    } else {
                        wsString += ((char) a);
                    }
                }
                break;
            case UNKNOWN:
                break;
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
                if (looped == a.length)
                    out.append(b);
                else
                    out.append(b).append(",");
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
        byte[] outBytes = new byte[0];
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(packet);
            out.flush();
            outBytes = bos.toByteArray();
        } catch (IOException e) {
            System.err.println("UNABLE TO DECODE PACKET!!!");
            System.err.println("Error: ");
            e.printStackTrace();
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
