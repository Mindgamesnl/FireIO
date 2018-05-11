package io.fire.core.common.eventmanager.objects;

import io.fire.core.common.eventmanager.interfaces.EventPayload;

import lombok.Getter;

public class Event {

    @Getter private String event;
    @Getter private EventPayload payload;

    public Event(String event, EventPayload payload) {

    }

}
