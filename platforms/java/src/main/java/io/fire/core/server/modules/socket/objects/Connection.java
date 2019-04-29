package io.fire.core.server.modules.socket.objects;

import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.driver.HandshakeDriver;
import io.fire.core.server.modules.socket.interfaces.NetworkDriver;

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
        System.out.println("open");
        driver.onOpen();
    }

    public void onClose() {
        System.out.println("Closed");
        driver.onClose();
    }

    public void onError() {
        System.out.println("error");
        driver.onError();
    }

    public void onData(byte[] data, int fufilled) {
        driver.onData(data, fufilled);
    }

}
