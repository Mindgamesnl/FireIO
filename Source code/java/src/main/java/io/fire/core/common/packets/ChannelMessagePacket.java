package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ChannelMessagePacket extends Packet implements Serializable {

    //trigger channel with a string
    //commonly used by server and client

    //channel to trigger
    private String channel;
    //payload
    private String text;

}
