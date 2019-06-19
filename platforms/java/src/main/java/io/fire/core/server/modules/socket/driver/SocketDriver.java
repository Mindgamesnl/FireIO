package io.fire.core.server.modules.socket.driver;

import io.fire.core.common.io.socket.interfaces.GenericClient;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.interfaces.Packet;
import io.fire.core.server.FireIoServer;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.server.modules.socket.objects.Client;
import io.fire.core.server.modules.socket.objects.Connection;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketDriver implements NetworkDriver {

    private Socket socket;
    private FireIoServer main;
    private Connection connection;
    private GenericClient genericClient;

    SocketDriver(Socket socket, FireIoServer main, Connection connection) {
        this.socket = socket;
        this.main = main;
        this.connection = connection;
        this.genericClient = new Client(this);
    }

    @Override
    public void onError() {
        System.out.println("Error");
    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {
        System.out.println("Close");
    }

    @Override
    public void onData(byte[] data, Integer length) {
        try {
            Packager packager = new Packager(new String(data));

            if (packager.isInternal()) {
                //TODO: handle internal packet
                Packet packet = (Packet) packager.getBodyAsObject();
                System.out.println("Received auth packet");
            }
            else if (packager.hasStringBody()) {
                // handle string with event
                main.getEventHandler().triggerTextChannel(genericClient, packager.getChannel(), packager.getBodyAsString());
            }
            else if (packager.hasPacketBody()) {
                // handle packet with event
                main.getEventHandler().triggerPacket(genericClient, packager.getChannel(), (Packet) packager.getBodyAsObject());
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
