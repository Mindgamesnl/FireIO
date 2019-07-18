package io.fire.core.server.modules.socket.objects;

import io.fire.core.common.io.socket.interfaces.GenericClient;
import io.fire.core.server.modules.socket.driver.SocketDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientManager {

    private Map<UUID, GenericClient> clientMap = new HashMap<>();

    public void register(SocketDriver socketDriver) {
        clientMap.put(socketDriver.getClientDetails().getUuid(), socketDriver.getGenericClient());
    }

    public void unlink(SocketDriver socketDriver) {
        clientMap.remove(socketDriver.getClientDetails().getUuid());
    }

    public GenericClient getClientById(UUID uuid) {
        return clientMap.get(uuid);
    }
}
