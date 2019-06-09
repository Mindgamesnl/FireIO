package io.fire.core.server.modules.socket.driver;

import io.fire.core.common.io.http.objects.HttpContent;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.packets.EmptyPacket;
import io.fire.core.server.FireIoServer;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.server.modules.socket.objects.Connection;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.Timer;
import java.util.TimerTask;

public class SocketDriver implements NetworkDriver {

    private Socket socket;
    private FireIoServer main;
    private Connection connection;

    SocketDriver(Socket socket, FireIoServer main, Connection connection) {
        this.socket = socket;
        this.main = main;
        this.connection = connection;
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
            HttpContent httpContent = new HttpContent(new String(data));
            System.out.println(new String(data));
            System.out.println("sending shit");


            try {
                send(new Packager("test terug", new EmptyPacket()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void send(Packager packager) throws IOException {
        this.socket.getChannel().write(ByteBuffer.wrap(packager.getBytes()));
    }
}
