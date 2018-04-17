package io.fire.core.common.events;

import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.interfaces.CompletePayload;
import io.fire.core.common.events.interfaces.EventPayload;
import io.fire.core.common.events.interfaces.GlobalListener;
import io.fire.core.common.events.interfaces.Listener;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@NoArgsConstructor
public class EventHandler {

    private Map<String, ConcurrentLinkedQueue<Listener>> events = new ConcurrentHashMap();
    private Map<Event, ConcurrentLinkedQueue<Listener>> systemEvents = new ConcurrentHashMap();
    private List<GlobalListener> globalListeners = new ArrayList<>();

    public void fireEvent(String event, EventPayload payload) {
        if (globalListeners.size() > 0) {
            CompletePayload completePayload = new CompletePayload();
            completePayload.setIsEvent(false);
            completePayload.setChannel(event);
            completePayload.setOriginalPayload(payload);
            globalListeners.forEach(gl -> gl.call(completePayload));
        }

        if (events.containsKey(event)) {
            for (Listener l : events.get(event)) {
                l.call(payload);
            }
        }
    }

    public void fireEvent(Event event, EventPayload payload) {
        if (globalListeners.size() > 0) {
            CompletePayload completePayload = new CompletePayload();
            completePayload.setIsEvent(true);
            completePayload.setEvent(event);
            completePayload.setChannel(event.toString());
            completePayload.setOriginalPayload(payload);
            globalListeners.forEach(gl -> gl.call(completePayload));
        }

        if (systemEvents.containsKey(event)) {
            for (Listener l : systemEvents.get(event)) {
                l.call(payload);
            }
        }
    }

    public EventHandler on(GlobalListener globalListener) {
        globalListeners.add(globalListener);
        return this;
    }

    public EventHandler on(String event, Listener listener) {
        ConcurrentLinkedQueue callbacks = events.get(event);
        if (callbacks == null) {
            callbacks = new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue tempCallbacks = events.putIfAbsent(event, callbacks);
            if (tempCallbacks != null) {
                callbacks = tempCallbacks;
            }
        }
        callbacks.add(listener);
        return this;
    }

    public EventHandler on(Event e, Listener listener) {
        ConcurrentLinkedQueue callbacks = systemEvents.get(e);
        if (callbacks == null) {
            callbacks = new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue tempCallbacks = systemEvents.putIfAbsent(e, callbacks);
            if (tempCallbacks != null) {
                callbacks = tempCallbacks;
            }
        }
        callbacks.add(listener);
        return this;
    }

}
