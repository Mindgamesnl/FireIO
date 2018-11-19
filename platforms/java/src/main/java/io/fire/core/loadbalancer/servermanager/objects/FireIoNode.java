package io.fire.core.loadbalancer.servermanager.objects;

import io.fire.core.loadbalancer.servermanager.enums.NodeState;
import io.fire.core.server.modules.client.superclasses.Client;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class FireIoNode {

    /**
     * A connected node to the network of servers
     */

    private Client pipe;
    private int connections;
    private UUID uuid;
    private NodeState state = NodeState.WAITING;
    private String host;
    private int port;
    private Instant setup = Instant.now();
    public int restInteractions = 0;

    public FireIoNode(UUID id) {
        this.uuid = id;
    }

}
