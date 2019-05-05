package io.fire.core.client.modules.socket;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.objects.ClientDetails;
import lombok.Getter;

public class SocketModule {

    private FireIoClient client;
    @Getter private ClientDetails clientDetails = new ClientDetails();

    public SocketModule(FireIoClient client) {

    }

}
