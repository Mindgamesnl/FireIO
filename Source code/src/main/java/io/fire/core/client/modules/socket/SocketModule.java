package io.fire.core.client.modules.socket;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.handlers.AsyncConnectionHandler;
import io.fire.core.common.interfaces.ClientMeta;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

public class SocketModule {

    private FireIoClient client;
    @Getter private AsyncConnectionHandler connection;
    private Thread connectionHandlerThread;

    public SocketModule(FireIoClient c, String host, int port, UUID id, Map<String, String> arguments, Map<String, ClientMeta> meta) {
        client = c;
        connection = new AsyncConnectionHandler(client, host, port, id, arguments, meta);
    }

}
