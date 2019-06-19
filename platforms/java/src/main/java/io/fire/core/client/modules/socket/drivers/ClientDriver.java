package io.fire.core.client.modules.socket.drivers;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.interfaces.Packet;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

@AllArgsConstructor
public class ClientDriver implements NetworkDriver {

    private FireIoClient fireIoClient;
    private Socket socket;

    @Override
    public void onError() {

    }

    @Override
    public void onOpen() {
        // send authentication packet
        try {
            send(new Packager(fireIoClient.getSocketModule().getClientDetails()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onData(byte[] data, Integer length) {
        try {
            Packager packager = new Packager(new String(data));

            // decide what packet type and what to do with it
            if (packager.isInternal()) {
                //TODO: handle internal packet
                Packet packet = (Packet) packager.getBodyAsObject();

            }
            else if (packager.hasStringBody()) {
                // handle string event
                fireIoClient.getEventHandler().triggerTextChannel(packager.getChannel(), packager.getBodyAsString());
            }
            else if (packager.hasPacketBody()) {
                // handle packet with event
                fireIoClient.getEventHandler().triggerPacket((Packet) packager.getBodyAsObject(), packager.getChannel());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Packager packager) throws IOException {
        this.socket.getChannel().write(ByteBuffer.wrap(packager.getBytes()));
    }

    public void send(String channel, Packet packet) throws IOException {
        send(new Packager(channel, packet));
    }

    public void send(String channel, String packet) throws IOException {
        send(new Packager(channel, packet));
    }
}
