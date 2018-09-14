package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PingPacket extends Packet {

    private Instant sendTime = Instant.now();

}
