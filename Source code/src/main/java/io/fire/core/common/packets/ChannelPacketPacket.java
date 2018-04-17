package io.fire.core.common.packets;

import io.fire.core.common.events.interfaces.EventPayload;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.server.modules.client.superclasses.Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelPacketPacket extends Packet implements EventPayload {

    private Client sender;
    private String channel;
    private Packet packet;


}
