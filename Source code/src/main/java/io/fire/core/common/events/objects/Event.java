package io.fire.core.common.events.objects;

import io.fire.core.common.events.interfaces.EventPayload;

import lombok.Getter;

public class Event {

    @Getter private String event;
    @Getter private EventPayload payload;

    public Event(String event, EventPayload payload) {

    }

}
