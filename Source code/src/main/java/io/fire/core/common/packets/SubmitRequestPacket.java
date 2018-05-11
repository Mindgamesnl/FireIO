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

    //the client created a new request for the server
    //commonly used by server and client
    //only send by client

    //request id, for callback
    private UUID requestId;
    //id, kinda like request name or channel
    private String id;
    //request payload, contains payload from client to server (for example, a string or query)
    private RequestBody payload;

}
