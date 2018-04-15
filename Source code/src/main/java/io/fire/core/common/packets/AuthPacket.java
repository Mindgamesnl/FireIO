package io.fire.core.common.packets;

import io.fire.core.common.interfaces.ClientMeta;
import io.fire.core.common.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthPacket extends Packet implements Serializable {

    private String uuid;
    private String platform;
    private Map<String, String> arguments;
    private Map<String, ClientMeta> argumentsMeta;

}
