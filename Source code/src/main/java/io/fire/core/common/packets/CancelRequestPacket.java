package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CancelRequestPacket extends Packet {

    private UUID request;

}
