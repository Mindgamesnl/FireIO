package io.fire.core.common.packets;

import io.fire.core.common.interfaces.Packet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateByteArraySize extends Packet {

    private Integer size;

}
