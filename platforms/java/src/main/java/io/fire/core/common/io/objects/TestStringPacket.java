package io.fire.core.common.io.objects;

import io.fire.core.common.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TestStringPacket extends Packet {

    private String string;

}
