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

    //packet that requests or forces the network to update the byte buffer size for socket communication
    //commonly used by server and client

    //when the client sends it it acts as a request
    //when send by server it is an order to update

    //new size
    private Integer size;

}
