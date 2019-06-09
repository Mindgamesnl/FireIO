package io.fire.core.client.modules.socket.drivers;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;
import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.packets.EmptyPacket;
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
        try {
            Packager packager = new Packager("test", new EmptyPacket());
            send(packager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose() {

    }

    @Override
    public void onData(byte[] data, Integer length) {
        System.out.println(new String(data));
    }

    public void send(Packager packager) throws IOException {
        this.socket.getChannel().write(ByteBuffer.wrap(packager.getBytes()));
    }
}
