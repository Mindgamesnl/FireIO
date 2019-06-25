package io.fire.core.client.modules.socket;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.handlers.OutgoingConnection;
import io.fire.core.client.modules.socket.objects.ClientDetails;
import lombok.Getter;

import java.io.IOException;

public class SocketModule {

    private FireIoClient client;
    @Getter private ClientDetails clientDetails = new ClientDetails();
    @Getter private OutgoingConnection connection;

    public SocketModule(FireIoClient client) {
        this.client = client;
    }

    public void startConnection() {
        try {
            this.connection = new OutgoingConnection(this.client);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
