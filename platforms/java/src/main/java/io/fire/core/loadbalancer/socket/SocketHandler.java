package io.fire.core.loadbalancer.socket;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.loadbalancer.FireIoBalancer;
import io.fire.core.loadbalancer.servermanager.enums.NodeState;
import io.fire.core.loadbalancer.servermanager.objects.FireIoNode;
import io.fire.core.server.modules.client.superclasses.Client;

public class SocketHandler {

    public SocketHandler(FireIoBalancer balancer) {
        balancer.getBalancingServer().on(Event.CONNECT, eventPayload -> {
            Client client = (Client) eventPayload;
            FireIoNode node = balancer.getServerManager().getNode(client);

            //reset node state
            node.setConnections(0);
            node.setState(NodeState.IDLE);
        });

        balancer.getBalancingServer().on(Event.DISCONNECT, eventPayload -> {
            Client client = (Client) eventPayload;
            FireIoNode node = balancer.getServerManager().getNode(client);

            //reset node state
            node.setConnections(0);
            node.setState(NodeState.DEAD);
        });

        balancer.getBalancingServer().on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
            Client client = (Client) eventPayload;
            FireIoNode node = balancer.getServerManager().getNode(client);

            //reset node state
            node.setConnections(0);
            node.setState(NodeState.DEAD);
        });
    }

}
