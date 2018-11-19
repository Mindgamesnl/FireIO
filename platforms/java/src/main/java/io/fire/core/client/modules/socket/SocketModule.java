package io.fire.core.client.modules.socket;

import io.fire.core.client.FireIoClient;
import io.fire.core.client.modules.socket.handlers.AsyncConnectionHandler;
import io.fire.core.common.interfaces.ClientMeta;

import lombok.Getter;

import java.util.Map;
import java.util.UUID;

public class SocketModule {

    @Getter private AsyncConnectionHandler connection;

    /**
     * Setup and start the ConnectionHandler
     *
     * @param client
     * @param host
     * @param port
     * @param id
     * @param arguments
     * @param meta
     */
    public SocketModule(FireIoClient client, String host, int port, UUID id, Map<String, String> arguments, Map<String, ClientMeta> meta) {
        //create connection handler with parameters and data
        connection = new AsyncConnectionHandler(client, host, port, id, arguments, meta);
    }

}
