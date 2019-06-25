package io.fire.core.server.modules.socket.driver;

import io.fire.core.client.modules.socket.objects.ClientDetails;
import io.fire.core.common.io.socket.interfaces.GenericClient;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.interfaces.Packet;
import io.fire.core.common.io.socket.packets.TestPacket;
import io.fire.core.server.FireIoServer;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.server.modules.socket.objects.Client;
import io.fire.core.server.modules.socket.objects.Connection;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class SocketDriver implements NetworkDriver {

    private Socket socket;
    private FireIoServer main;
    private Connection connection;
    private GenericClient genericClient;
    private ClientDetails clientDetails;
    private Queue<Packager> packetQueue = new LinkedList<>();
    private int packetsWithoutConfirmation = 0;
    private int count = 0;

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

                // check if packet marks op_allow_write
                if (packager.getHeader("-f-op").equals("op_allow_write")) {
                    packetsWithoutConfirmation--;
                    if (this.packetQueue.size() != 0) this.socket.getChannel().write(packetQueue.poll().getBuffer());
                    return;
                }

                //TODO: handle internal packet
                Packet packet = (Packet) packager.getPayloadAsObject();

                if (packet instanceof ClientDetails) {
                    // apply client details
                    clientDetails = (ClientDetails) packet;

                    // check if password is correct, and optional other stuff
                }
            }
            else if (packager.hasStringBody()) {
                // handle string with event
                main.getEventHandler().triggerTextChannel(genericClient, packager.getChannel(), packager.getBodyAsString());
            }
            else if (packager.hasPacketBody()) {
                // handle packet with event
                main.getEventHandler().triggerPacket(genericClient, packager.getChannel(), (Packet) packager.getPayloadAsObject());
            }

            // message that we can get the new packet if the other side had a queue
            this.socket.getChannel().write(new Packager().setHeader("-f-op", "op_allow_write").getBuffer());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Packager packager) throws IOException {
        if (packetQueue.size() == 0 && packetsWithoutConfirmation == 0) {
            packetsWithoutConfirmation++;
            this.socket.getChannel().write(packager.getBuffer());
        } else {
            this.packetQueue.add(packager);
        }
    }

    public void send(String channel, Packet packet) throws IOException {
        send(new Packager(channel, packet));
    }

    public void send(String channel, String packet) throws IOException {
        send(new Packager(channel, packet));
    }
}
