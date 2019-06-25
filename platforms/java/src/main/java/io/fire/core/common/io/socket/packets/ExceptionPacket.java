package io.fire.core.common.io.socket.packets;

import io.fire.core.common.io.socket.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExceptionPacket extends Packet {

    private Exception exception;

}
