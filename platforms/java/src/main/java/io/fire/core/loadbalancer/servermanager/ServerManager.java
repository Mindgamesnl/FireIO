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
import java.util.function.BinaryOperator;
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
                    } else {
                        n.setRestInteractions(0);
                    }
                });
            }
        }, 0, 1000);
    }


    public FireIoNode getAvailableServer() {
        return nodes.values().stream()
                .filter(node -> node.getState() == NodeState.CONNECTED)
                .reduce(BinaryOperator.minBy(Comparator.comparing(FireIoNode::getConnections)))
                .orElse(null);
    }


    public FireIoNode getAvailableEndpoint() {
        return nodes.values().stream()
                .filter(node -> node.getState() == NodeState.CONNECTED)
                .reduce(BinaryOperator.minBy(Comparator.comparing(FireIoNode::getRestInteractions)))
                .orElse(null);
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