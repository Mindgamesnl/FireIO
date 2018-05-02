package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.RequestBody;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmitRequestPacket extends Packet {

    private UUID requestId;
    private String id;
    private RequestBody payload;

}
