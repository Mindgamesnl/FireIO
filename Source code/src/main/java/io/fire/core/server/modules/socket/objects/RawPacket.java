package io.fire.core.server.modules.socket.objects;

import io.fire.core.common.events.interfaces.EventPayload;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RawPacket implements EventPayload {

    private String data;

}
