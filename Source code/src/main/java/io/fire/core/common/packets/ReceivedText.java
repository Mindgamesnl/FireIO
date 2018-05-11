package io.fire.core.common.packets;

import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.server.modules.client.superclasses.Client;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceivedText implements EventPayload {

    //received text payload
    //used to signal text via the evenmanager
    //commonly used by server and client

    //text
    private String string;
    //the sender value is null since the server fills it in with the object for this client for easy back and forwards communication via the event system
    private Client sender;

}
