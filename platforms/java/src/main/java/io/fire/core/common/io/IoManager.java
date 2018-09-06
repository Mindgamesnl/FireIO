package io.fire.core.common.io;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.PoolHolder;
import lombok.Setter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
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
    private StringBuilder wsDataStream = new StringBuilder();
    @Setter private WebSocketStatus webSocketStatus = WebSocketStatus.IDLE_NEW;

    //protocol type
    private Boolean hasReceived = false;
    private IoType ioType = IoType.UNKNOWN;

    @Setter private Consumer<Packet> packetHandler = (p) -> {};
    @Setter private Consumer<WebSocketTransaction> webSocketHandler = (p) -> {};

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
                //add to data
                if (webSocketStatus == WebSocketStatus.IDLE_NEW) {
                    wsDataStream.append(new String(input));
                    String data = wsDataStream.toString();
                    //does it end here?
                    if (data.contains("\r\n\r\n")) {
                        //does it have other parts?
                        if (data.split("\r\n\r\n").length == 1) {
                            poolHolder.getPool().run(() -> webSocketHandler.accept(new WebSocketTransaction(data.split("\r\n\r\n")[0], webSocketStatus)));
                        } else {
                            wsDataStream = new StringBuilder();
                            wsDataStream.append(data.split("\r\n\r\n")[1]);
                        }
                    }
                } else if (webSocketStatus == WebSocketStatus.CONNECED) {
                    String data = new String(parseEncodedFrame(input).getPayload(), Charset.defaultCharset());
                    poolHolder.getPool().run(() -> webSocketHandler.accept(new WebSocketTransaction(data, webSocketStatus)));
                }
                break;

            case UNKNOWN:
                break;
        }
    }

    private WebSocketFrame parseEncodedFrame(byte[] raw) {
        ByteBuffer buf = ByteBuffer.wrap(raw);
        WebSocketFrame frame = new WebSocketFrame();
        byte b = buf.get();
        frame.setFin(((b & 0x80) != 0));
        frame.setOpcode((byte)(b & 0x0F));

        b = buf.get();
        boolean masked = ((b & 0x80) != 0);
        int payloadLength = (byte)(0x7F & b);
        int byteCount = 0;
        if (payloadLength == 0x7F) byteCount = 8;
        else if (payloadLength == 0x7E) byteCount = 2;

        while (--byteCount > 0) {
            b = buf.get();
            payloadLength |= (b & 0xFF) << (8 * byteCount);
        }

        byte maskingKey[] = null;
        if (masked) {
            maskingKey = new byte[4];
            buf.get(maskingKey,0,4);
        }

        frame.setPayload(new byte[payloadLength]);
        buf.get(frame.getPayload(),0,payloadLength);

        if (masked) for (int i = 0; i < frame.getPayload().length; i++) frame.getPayload()[i] ^= maskingKey[i % 4];
        return frame;
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

    public void write(ByteBuffer buffer) throws IOException {
        write(new ByteBuffer[]{buffer});
    }

    private void write(ByteBuffer[] buffers) throws IOException {
        for (ByteBuffer buffer : buffers) {
            this.channel.write(buffer);
            buffer.clear();
        }
    }

}
