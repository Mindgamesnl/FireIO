package io.fire.core.client.modules.socket;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.handlers.AsyncConnectionHandler;
import io.fire.core.common.interfaces.ClientMeta;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

public class SocketModule {

    @Getter private AsyncConnectionHandler connection;

    public SocketModule(FireIoClient c, String host, int port, UUID id, Map<String, String> arguments, Map<String, ClientMeta> meta) {
        //create connection handler with parameters and data
        connection = new AsyncConnectionHandler(c, host, port, id, arguments, meta);
    }

}
