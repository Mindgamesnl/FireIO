package io.fire.core.client;

import io.fire.core.client.modules.socket.SocketModule;
import io.fire.core.client.modules.socket.objects.Host;
import io.fire.core.common.events.EventHandler;
import lombok.Getter;

public class FireIoClient {

    @Getter private Host host;

    @Getter private EventHandler eventHandler = new EventHandler();
    @Getter private SocketModule socketModule;

    public FireIoClient(String host, int port) {
        this.host = new Host(host, port);
        this.socketModule = new SocketModule(this);
    }

    public FireIoClient setPassword(String password) {
        socketModule.getClientDetails().setPassword(password);
        return this;
    }

    public FireIoClient connect() {
        socketModule.startConnection();
        return this;
    }

}
