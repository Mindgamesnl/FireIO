package io.fire.core.common.io;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.PoolHolder;
import io.fire.core.common.io.api.request.PendingRequest;
import io.fire.core.common.io.enums.*;
import io.fire.core.common.io.frames.FrameData;
import io.fire.core.common.io.http.enums.HttpContentType;
import io.fire.core.common.io.http.enums.HttpRequestMethod;
import io.fire.core.common.io.http.enums.HttpStatusCode;
import io.fire.core.common.io.http.objects.HttpContent;
import io.fire.core.common.io.objects.*;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.enums.BlockedProtocol;

import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

public class IoManager {

    //socket
    private SocketChannel channel;

    //buffer for the fireio protocol
    private IoFrameSet frameSet = null;

    //buffers for the websocket protocol
    private WebsocketHandler weboscketUtil = new WebsocketHandler();
    private StringBuilder wsDataStream = new StringBuilder();
    @Setter private WebSocketStatus webSocketStatus = WebSocketStatus.IDLE_NEW;

    //protocol type
    @Getter private ConnectionType ioType = ConnectionType.NONE;
    private Boolean hasReceived = false;

    //handlers
    @Setter private Consumer<Packet> packetHandler = (p) -> {};
    @Setter private Consumer<WebSocketTransaction> webSocketHandler = (p) -> {};

    //packet buffers
    private Queue<ByteBuffer> queuedFrames = new ConcurrentLinkedDeque<>();
    private boolean isChannelLocked = false;

    //runner
    private InstanceSide side;
    @Getter private FireIoServer server;
    private FireIoClient client;


    /**
     * Setup the IoManager instance
     *
     * @param channel
     * @param side
     * @param parent
     */
    public IoManager(SocketChannel channel, InstanceSide side, Object parent) {
        this.channel = channel;
        this.side = side;
        if (this.side == InstanceSide.SERVER) {
            server = (FireIoServer) parent;
        } else {
            client = (FireIoClient) parent;
        }
    }


    /**
     * Handle data that was received by the socket.
     * This handles ALL data, it decides what protocol to use, when and what to do with it.
     *
     * @param input
     * @param poolHolder
     * @param length
     */
    public void handleData(byte[] input, PoolHolder poolHolder, int length) {
        String requestAsString = new String(input);
        if (!hasReceived) {
            if (HttpRequestMethod.isHttp(requestAsString)) {
                this.ioType = ConnectionType.HTTP;
            } else {
                requestAsString = null;
                this.ioType = ConnectionType.FIREIO;
            }
            hasReceived = true;
        }

        switch (this.ioType) {
            case FIREIO:
                if (side == InstanceSide.SERVER) {
                    if (server.getSocketModule().getBlockedProtocolList().contains(BlockedProtocol.FIREIO)) {
                        try {
                            server.getSocketModule().getAsyncNetworkService().getSelectorHandler().getReferences().remove(channel.socket().getRemoteSocketAddress());
                            channel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                }

                if (frameSet == null) frameSet = new IoFrameSet();
                try {
                    frameSet.readInput(input);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (frameSet.isFinished()) {
                    if (frameSet.getFirstType() == IoFrameType.CONFIRM_PACKET) {
                        //it is a confermation, dont trigger it but handle it here
                        frameSet = new IoFrameSet();
                        handlePacketConfirmation();
                        return;
                    } else {
                        //it is a normal payload, trigger it
                        try {
                            packetHandler.accept(frameSet.getPayload());
                        } catch (Exception e) {
                            System.err.println("[Fire-IO] Packet event handler caused an exception.");
                            e.printStackTrace();
                        }
                        //let the other side know that it may send a new packet
                        forceWrite(new IoFrameSet(IoFrameType.CONFIRM_PACKET).getFrames().get(0).getBuffer(), false);
                        frameSet = new IoFrameSet();
                    }
                }
                break;

            case HTTP: {
                HttpContent headers = new HttpContent(requestAsString);
                //handle http input
                //is there a websocket upgrade packet? than resume as a websocket connection
                if (!headers.getHeader("Sec-WebSocket-Key").equals("") && !headers.getHeader("Connection").equals("")) {
                    if (side == InstanceSide.SERVER) {
                        if (server.getSocketModule().getBlockedProtocolList().contains(BlockedProtocol.WEBSOCKET)) {
                            try {
                                channel.close();
                                server.getSocketModule().getAsyncNetworkService().getSelectorHandler().getReferences().remove(channel.socket().getRemoteSocketAddress());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }
                    //how lovely, its an upgread request, so that means, that this is a real socket! well for fucks sake...
                    String finalRequestAsString = requestAsString;
                    webSocketHandler.accept(new WebSocketTransaction(finalRequestAsString.split("\r\n\r\n")[0], webSocketStatus));
                    this.ioType = ConnectionType.WEBSOCKET;
                    return;
                } else {
                    if (side == InstanceSide.SERVER) {
                        if (server.getSocketModule().getBlockedProtocolList().contains(BlockedProtocol.HTTP)) {
                            try {
                                channel.close();
                                server.getSocketModule().getAsyncNetworkService().getSelectorHandler().getReferences().remove(channel.socket().getRemoteSocketAddress());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return;
                        }
                    }


                    //handle http
                    poolHolder.getPool().run(() -> {
                        try {
                            server.getHttpModule().getHttpRequestProcessor().handle(new PendingRequest(this, headers, channel));
                        } catch (IOException e) {
                            //create error page
                            String page = server.getHttpModule().getHttpResources().get("500.html").replace("{{stacktrace-message}}", e.getClass().getName() + ": " + e.getMessage());
                            HttpContent errorPage = new HttpContent(HttpContentType.HTML, HttpStatusCode.C_500);
                            errorPage.setBody(page);
                            forceWrite(errorPage.getBuffer(), false);
                            try {
                                channel.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            server.getSocketModule().getAsyncNetworkService().getSelectorHandler().getReferences().remove(channel.socket().getRemoteSocketAddress());
                            e.printStackTrace();
                        }
                    });
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
                            webSocketHandler.accept(new WebSocketTransaction(data.split("\r\n\r\n")[0], webSocketStatus));
                        } else {
                            wsDataStream = new StringBuilder();
                            wsDataStream.append(data.split("\r\n\r\n")[1]);
                        }
                    }
                } else if (webSocketStatus == WebSocketStatus.CONNECED) {
                    String data = new String(weboscketUtil.parseEncodedFrame(input).getPayload(), Charset.defaultCharset());
                    webSocketHandler.accept(new WebSocketTransaction(data, webSocketStatus));
                }
                break;

            case NONE:
                break;
        }

    }


    /**
     * Create and send a websocket packet
     *
     * @param str
     * @throws IOException
     */
    public void sendWebSocket(String str) throws IOException {
        FrameData currentFrame = new FrameData(Opcode.TEXT);
        currentFrame.setPayload(ByteBuffer.wrap(str.getBytes("UTF-8")));
        currentFrame.setTransferemasked(false);
        List<FrameData> out = Collections.singletonList(currentFrame);
        for (FrameData fd : out) this.channel.write((ByteBuffer) weboscketUtil.parseData(fd).flip());
    }


    /**
     * Send a packet, this parses it to individual Fire-Io-Protocol-Frames and requests to send them
     *
     * @param p
     */
    public void send(Packet p) {
        IoFrameSet emitter = null;
        try {
            emitter = new IoFrameSet(p);
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (IoFrame frame : emitter.getFrames()) {
            proposeWrite(frame.getBuffer());
        }
    }


    /**
     * Propose the writing of data.
     * This checks if the channel is ready.
     * If ready send it immediately, else add it to a que for later sending in the correct order
     *
     * @param content
     */
    private void proposeWrite(ByteBuffer content) {
        if (queuedFrames.size() == 0 || !isChannelLocked) {
            forceWrite(content, true);
        } else {
            queuedFrames.add(content);
        }
    }


    /**
     * Force write a packet over the channel.
     * Blocks the que if necessary
     *
     * @param wrap
     * @param requiresLock
     */
    public void forceWrite(ByteBuffer wrap, boolean requiresLock) {
        if (this.channel.isOpen()) {
            try {
                if (requiresLock) isChannelLocked = true;
                this.channel.write(wrap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        wrap.clear();
    }


    /**
     * Handles a confirmation of the packet from the other side.
     * This resets the lock and triggers the FIRST and longest waiting que object to send
     */
    private void handlePacketConfirmation() {
        isChannelLocked = false;
        if (queuedFrames.size() != 0) {
            forceWrite(queuedFrames.poll(), true);
        }
    }
}
