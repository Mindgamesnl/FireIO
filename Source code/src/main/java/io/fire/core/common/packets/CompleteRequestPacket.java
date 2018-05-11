package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.interfaces.RequestBody;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class CompleteRequestPacket extends Packet {

    //complete a pending client-side request
    //commonly used by server and client

    //request id
    private UUID requestId;
    //request response (can be null)
    private RequestBody result;

}
