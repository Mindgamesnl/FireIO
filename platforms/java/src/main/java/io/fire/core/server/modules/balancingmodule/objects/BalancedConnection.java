package io.fire.core.server.modules.balancingmodule.objects;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.server.FireIoServer;
import lombok.Getter;

import java.util.UUID;

public class BalancedConnection {

    private FireIoClient fireIoClient;
    private boolean isReady = false;
    @Getter private UUID id;


    /**
     * A connection with a loadbalancer, used to register and update status
     *
     * @param main
     * @param hos
     * @param port
     * @param password
     */
    public BalancedConnection(FireIoServer main, String hos, int port, String password) {
        //setup custom fireio client
        fireIoClient = new FireIoClient(hos, port);
        fireIoClient.getRestModule().setOptionalPath("fireio/balancer/register/" + main.getPort());
        if (password != null) fireIoClient.setPassword(password);
        fireIoClient.setAutoReConnect(1000);

        main.on(Event.DISCONNECT, eventPayload -> {
            if (isReady) fireIoClient.send("removeuser", "");
        });

        main.on(Event.CONNECT, eventPayload -> {
            if (isReady) fireIoClient.send("adduser", "");
        });

        fireIoClient.on(Event.CONNECT, eventPayload -> {
            id = fireIoClient.getSocketModule().getConnection().getId();
            isReady = true;
        });

        fireIoClient.on(Event.TIMED_OUT, eventPayload -> {
            isReady = false;
        });

        fireIoClient.on(Event.DISCONNECT, eventPayload -> {
            isReady = false;
        });

        fireIoClient.connect();
    }

}
