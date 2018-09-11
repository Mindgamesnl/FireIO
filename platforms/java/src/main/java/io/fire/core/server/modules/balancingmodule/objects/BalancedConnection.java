package io.fire.core.server.modules.balancingmodule.objects;

import io.fire.core.client.FireIoClient;
import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.server.FireIoServer;
import lombok.Getter;

import java.util.UUID;

public class BalancedConnection implements EventPayload {

    private FireIoClient fireIoClient;
    private boolean isReady = false;
    @Getter private UUID id;

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
            main.getEventHandler().fireEvent(Event.LOAD_BALANCER_LINKED, this);
            id = fireIoClient.getSocketModule().getConnection().getId();
            isReady = true;
        });

        fireIoClient.on(Event.CLOSED_UNEXPECTEDLY, eventPayload -> {
            isReady = false;
        });

        fireIoClient.on(Event.DISCONNECT, eventPayload -> {
            main.getEventHandler().fireEvent(Event.LOAD_BALANCER_UNLINKED, this);
            isReady = false;
        });

        fireIoClient.connect();
    }

}
