package io.fire.core.server.modules.socket.driver;

import io.fire.core.common.io.http.enums.HttpRequestMethod;
import io.fire.core.server.FireIoServer;
import io.fire.core.server.modules.socket.interfaces.NetworkDriver;
import io.fire.core.server.modules.socket.objects.Connection;

import java.net.Socket;

public class HandshakeDriver implements NetworkDriver {

    private Connection connection;
    private Socket socket;
    private FireIoServer main;

    public HandshakeDriver(Connection connection, Socket socket, FireIoServer main) {
        this.connection = connection;
        this.socket = socket;
        this.main = main;
    }

    @Override
    public void onError() {

    }

    @Override
    public void onOpen() {

    }

    @Override
    public void onClose() {

    }

    @Override
    public void onData(byte[] data, Integer length) {
        String dataAsString = new String(data);
        this.connection.setDriver(HttpRequestMethod.isHttp(dataAsString) ? new HttpDriver(socket, main, this.connection) : new SocketDriver());
        this.connection.getDriver().onData(data, length);
    }
}
