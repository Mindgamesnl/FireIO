package io.fire.core.loadbalancer.socket;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.loadbalancer.FireIoBalancer;
import io.fire.core.loadbalancer.servermanager.enums.NodeState;
import io.fire.core.loadbalancer.servermanager.objects.FireIoNode;
import io.fire.core.server.modules.client.superclasses.Client;

public class SocketHandler {


    /**
     * Setup balancer event listeners
     *
     * @param balancer
     */
    public SocketHandler(FireIoBalancer balancer) {
        balancer.getBalancingServer().on(Event.CONNECT, eventPayload -> {
            Client client = (Client) eventPayload;
            FireIoNode node = balancer.getServerManager().getNode(client);

            //reset node state
            node.setConnections(0);
            node.setState(NodeState.CONNECTED);
            System.out.println("[Fire-IO-Balancer] Node connected, id="+node.getUuid().toString());
        });

        balancer.getBalancingServer().on("adduser", (sender, text) -> {
            FireIoNode node = balancer.getServerManager().getNode(sender);
            node.setConnections(node.getConnections() + 1);
        });

        balancer.getBalancingServer().on("removeuser", (sender, text) -> {
            FireIoNode node = balancer.getServerManager().getNode(sender);
            node.setConnections(node.getConnections() - 1);
        });

        balancer.getBalancingServer().on(Event.DISCONNECT, (sender, text) -> {
            FireIoNode node = balancer.getServerManager().getNode(sender);

            //reset node state
            node.setConnections(0);
            node.setState(NodeState.DEAD);
            System.out.println("[Fire-IO-Balancer] Node died, id="+node.getUuid().toString());
        });
    }

}
