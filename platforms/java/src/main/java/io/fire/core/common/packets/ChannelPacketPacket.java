package io.fire.core.common.packets;

import io.fire.core.common.eventmanager.interfaces.EventPayload;
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

    //trigger a channel with a custom packet
    //commonly used by client and server

    //the sender value is null since the server fills it in with the object for this client for easy back and forwards communication via the event system
    private Client sender;
    //the channel to trigger
    private String channel;
    //the custom packet
    private Packet packet;


}
