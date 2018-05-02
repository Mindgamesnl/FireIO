package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.RequestBody;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CompleteRequestPacket extends Packet {

    private UUID requestId;
    private RequestBody result;

}
