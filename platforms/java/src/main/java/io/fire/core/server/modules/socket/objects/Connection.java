package io.fire.core.server.modules.socket.objects;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.driver.HandshakeDriver;
import io.fire.core.common.io.socket.interfaces.NetworkDriver;

import lombok.Getter;
import lombok.Setter;
import java.net.Socket;

public class Connection {

    private Socket socket;
    private FireIoServer main;
    @Setter @Getter private NetworkDriver driver;

    public Connection(Socket socket, FireIoServer main) {
        this.socket = socket;
        this.main = main;
        this.driver = new HandshakeDriver(this, socket, main);
    }

    public void onOpen() {
        driver.onOpen();
    }

    public void onClose() {
        driver.onClose();
    }

    public void onError() {
        driver.onError();
    }

    public void onData(byte[] data, int fufilled) {
        driver.onData(data, fufilled);
    }

}
