package io.fire.core.common.packets;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.events.interfaces.EventPayload;
import io.fire.core.common.interfaces.ConnectedFireioClient;
import io.fire.core.common.interfaces.Packet;

import io.fire.core.server.modules.client.superclasses.Client;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPacketPacket extends Packet implements EventPayload {

    private Client sender;
    private String channel;
    private Packet packet;


}
