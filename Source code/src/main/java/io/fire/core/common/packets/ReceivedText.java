package io.fire.core.common.packets;

import io.fire.core.common.events.interfaces.EventPayload;
import io.fire.core.server.modules.client.superclasses.Client;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceivedText implements EventPayload {

    private String string;
    private Client sender;

}
