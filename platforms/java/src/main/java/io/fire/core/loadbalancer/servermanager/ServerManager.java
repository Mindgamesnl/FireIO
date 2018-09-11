package io.fire.core.loadbalancer.servermanager;

import io.fire.core.loadbalancer.FireIoBalancer;
import io.fire.core.loadbalancer.servermanager.enums.NodeState;
import io.fire.core.loadbalancer.servermanager.objects.FireIoNode;
import io.fire.core.server.modules.client.superclasses.Client;
import lombok.Getter;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerManager {

    @Getter private Map<UUID, FireIoNode> nodes = new ConcurrentHashMap<>();

    public ServerManager(FireIoBalancer balancer) {
        //clearance
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Instant now = Instant.now();
                nodes.values().forEach(n -> {
                    if (n.getState() != NodeState.CONNECTED) {
                        if (Duration.between(n.getSetup(), now).getSeconds() >= 5) {
                            nodes.remove(n.getUuid());
                        }
                    }
                });
            }
        }, 0, 1000);
    }

    public FireIoNode getAvalibleServer() {
        final int[] lowest = {-1};
        final FireIoNode[] selected = {null};
        nodes.values().stream()
                .filter(n -> n.getState() == NodeState.CONNECTED)
                .collect(Collectors.toList())
                .forEach(n -> {
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