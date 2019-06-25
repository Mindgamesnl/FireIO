package io.fire.core.common.io.socket.packets;

import io.fire.core.common.io.socket.interfaces.Packager;
import io.fire.core.common.io.socket.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

@Getter
@AllArgsConstructor
public class ClusterPacket extends Packet implements Serializable {

    private List<Packager> cluster;

}
