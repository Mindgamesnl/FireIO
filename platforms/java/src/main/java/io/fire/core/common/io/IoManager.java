package io.fire.core.common.io;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.PoolHolder;
import io.fire.core.common.io.enums.IoType;
import io.fire.core.common.io.enums.Opcode;
import io.fire.core.common.io.enums.WebSocketStatus;
import io.fire.core.common.io.frames.FrameData;
import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpRequestMethod;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.common.io.http.objects.HttpHeaders;
import io.fire.core.common.io.objects.IoFrame;
import io.fire.core.common.io.objects.IoFrameSet;
import io.fire.core.common.io.objects.WebSocketFrame;
import io.fire.core.common.io.objects.WebSocketTransaction;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Consumer;

public class IoManager {

    //socket
    private SocketChannel channel;

    //buffer for the fireio protocol
    private IoFrameSet frameSet = null;

    //buffers for the websocket protocol
    private StringBuilder wsDataStream = new StringBuilder();
    @Setter private WebSocketStatus webSocketStatus = WebSocketStatus.IDLE_NEW;

    //protocol type
    @Getter private IoType ioType = IoType.UNKNOWN;
    private Boolean hasReceived = false;

    @Setter private Consumer<Packet> packetHandler = (p) -> {};
    @Setter private Consumer<WebSocketTransaction> webSocketHandler = (p) -> {};

    public IoManager(SocketChannel channel) {
        this.channel = channel;
    }

    public void handleData(byte[] input, PoolHolder poolHolder, int length) {

        String requestAsString = new String(input);
        if (!hasReceived) {
            if (HttpRequestMethod.isHttp(requestAsString)) {
                this.ioType = IoType.HTTP;
            } else {
                requestAsString = null;
                this.ioType = IoType.FIREIO;
            }
            hasReceived = true;
        }

        switch (this.ioType) {
            case FIREIO:
                if (frameSet == null) frameSet = new IoFrameSet();
                try {
                    frameSet.readInput(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (frameSet.isFinished()) {
                    packetHandler.accept(frameSet.getPayload());
                    frameSet = new IoFrameSet();
                }
                break;

            case HTTP: {
                HttpHeaders headers = new HttpHeaders(requestAsString);

                //handle http input
                //is there a websocket upgrade packet? than resume as a websocket connection
                if (headers.getHeader("Sec-WebSocket-Key") != null && headers.getHeader("Connection") != null) {
                    //how lovely, its an upgread request, so that means, that this is a real socket! well for fucks sake...
                    String finalRequestAsString = requestAsString;
                    poolHolder.getPool().run(() -> webSocketHandler.accept(new WebSocketTransaction(finalRequestAsString.split("\r\n\r\n")[0], webSocketStatus)));
                    this.ioType = IoType.WEBSOCKET;
                    return;
                } else {
                    //handle http
                    System.out.println("Requst from " + headers.getUrl());
                    headers.getHeaders().forEach((key, value) -> {
                        System.out.println("Header k="+key + " v="+value);
                    });
                    System.out.println("Request type " + headers.getMethod());
                    System.out.println("Body is " + headers.getBody());
                    //so handle like a http transaction
                    HttpHeaders response = new HttpHeaders(HttpContentType.HTML, HttpStatusCode.C_200);
                    response.setBody("Je hebt het volgende tegen me gezegt: " + headers.getBody());

                    //prepare response
                    write(response.getBuffer());
                    try {
                        channel.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }

            case WEBSOCKET:
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
                }
                else if (webSocketStatus == WebSocketStatus.CONNECED) {
                    String data = new String(parseEncodedFrame(input).getPayload(), Charset.defaultCharset());
                    poolHolder.getPool().run(() -> webSocketHandler.accept(new WebSocketTransaction(data, webSocketStatus)));
                }
                break;

            case UNKNOWN:
                break;
        }
    }

    public void sendWebSocket(String str) throws IOException {
        FrameData currentFrame = new FrameData(Opcode.TEXT);
        currentFrame.setPayload(ByteBuffer.wrap(str.getBytes("UTF-8")));
        currentFrame.setTransferemasked(false);
        List<FrameData> out = Collections.singletonList(currentFrame);
        for (FrameData fd : out) this.channel.write((ByteBuffer) parseData(fd).flip());
    }

    private byte[] toByteArray(long val, int bytecount ) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for( int i = 0; i < bytecount; i++ ) {
            buffer[i] = ( byte ) ( val >>> ( highest - 8 * i ) );
        }
        return buffer;
    }

    private ByteBuffer parseData(FrameData framedata) {
        ByteBuffer mes = framedata.getPayloadData();
        int byteSize = mes.remaining() <= 125 ? 1 : mes.remaining() <= 65535 ? 2 : 8;
        ByteBuffer buf = ByteBuffer.allocate(1 + (byteSize > 1 ? byteSize + 1 : byteSize) + 0 + mes.remaining());
        byte opt = (byte) framedata.getOpcode().getId();
        byte one = (byte) (framedata.isFin() ? -128 : 0);
        one |= opt;
        buf.put(one);
        byte[] payload = toByteArray(mes.remaining(), byteSize);
        assert (payload.length == byteSize);
        if (byteSize == 1) {
            buf.put((byte) (payload[0] | 0));
        } else if (byteSize == 2) {
            buf.put((byte) ((byte) 126 | 0));
            buf.put(payload);
        } else if (byteSize == 8) {
            buf.put((byte) ((byte) 127 | 0));
            buf.put(payload);
        }
        buf.put(mes);
        mes.flip();
        assert (buf.remaining() == 0) : buf.remaining();
        buf.flip();
        return buf;
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


        System.out.println("get payload " + payloadLength + " whilst raw it is " + raw.length);
        frame.setPayload(new byte[payloadLength]);
        buf.get(frame.getPayload(),0, payloadLength);

        if (masked) for (int i = 0; i < frame.getPayload().length; i++) frame.getPayload()[i] ^= maskingKey[i % 4];
        return frame;
    }

    public void send(Packet p) {
        IoFrameSet emitter = null;
        try {
            emitter = new IoFrameSet(p);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (IoFrame frame : emitter.getFrames()) {
            write(frame.getBuffer());
        }
    }

    public void write(ByteBuffer wrap) {
        if (this.channel.isOpen()) {
            try {
                this.channel.write(wrap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wrap.clear();
    }
}
