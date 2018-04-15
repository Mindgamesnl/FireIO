package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateByteArraySize extends Packet {

    private Integer size;

}
