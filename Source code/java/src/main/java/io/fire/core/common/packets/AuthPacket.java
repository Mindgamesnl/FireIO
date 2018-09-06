package io.fire.core.common.packets;

import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.Packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AuthPacket extends Packet implements Serializable {

    //auth packet for handshake
    //contains client information, platform, uuid and arguments
    //commonly used by client and server

    private String uuid;
    private String platform;
    private Map<String, String> arguments;
    private Map<String, ClientMeta> argumentsMeta;

}
