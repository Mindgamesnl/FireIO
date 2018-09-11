package io.fire.core.loadbalancer.servermanager;

import io.fire.core.loadbalancer.FireIoBalancer;
import io.fire.core.loadbalancer.servermanager.enums.NodeState;
import io.fire.core.loadbalancer.servermanager.objects.FireIoNode;
import io.fire.core.server.modules.client.superclasses.Client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ServerManager {

    private Map<UUID, FireIoNode> nodes = new HashMap<>();

    public ServerManager(FireIoBalancer balancer) {

    }

    public FireIoNode getAvalibleServer() {
        final int[] lowest = {-1};
        final FireIoNode[] selected = {null};
        nodes.values().stream()
                .filter(n -> n.getState() == NodeState.CONNECTED)
                .collect(Collectors.toList())
                .forEach(n -> {https://twitter.com/MongoDB/status/1039191654274674690
                    if (lowest[0] == -1 || lowest[0] > n.getConnections()) {
                        selected[0] = n;
                        lowest[0] = n.getConnections();
                    }
                });
        return selected[0];
    }

    public FireIoNode create(UUID uuid) {
        FireIoNode node = new FireIoNode(uuid);
        nodes.put(uuid, node);
        return node;
    }

    public FireIoNode getNode(Client client) {
        return nodes.get(client.getId());
    }

}