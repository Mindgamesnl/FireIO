package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class ChannelMessagePacket extends Packet implements Serializable {

    private String channel;
    private String text;

}
