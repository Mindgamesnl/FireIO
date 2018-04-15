package io.fire.core.common.events.interfaces;

import io.fire.core.common.events.enums.Event;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class CompletePayload {

    private Event event;
    private Boolean isEvent;
    private EventPayload originalPayload;
    private String channel;

}
