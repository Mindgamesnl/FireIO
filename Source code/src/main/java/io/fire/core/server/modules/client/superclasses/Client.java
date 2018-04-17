package io.fire.core.server.modules.client.superclasses;

import io.fire.core.common.events.interfaces.EventPayload;
import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.ConnectedFireioClient;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.server.modules.client.objects.ClientInfo;

import lombok.Data;

import java.util.UUID;

@Data
public abstract class Client implements EventPayload, ConnectedFireioClient {

    private UUID id;
    public void send(String channel, String message) {}
    public void send(String channel, Packet packet) {}
    public String getTag(String key) {return null;}
    public ClientMeta getMeta(String key) {return null;}
    public void close() {}
    public ClientInfo getInfo() {return null;}

}
