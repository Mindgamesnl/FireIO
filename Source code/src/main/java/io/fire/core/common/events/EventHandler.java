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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

@NoArgsConstructor
public class EventHandler {

    private ConcurrentMap<String, ConcurrentLinkedQueue<Listener>> events = new ConcurrentHashMap();
    private List<GlobalListener> globalListeners = new ArrayList<>();
    private ConcurrentMap<Event, ConcurrentLinkedQueue<Listener>> systemEvents = new ConcurrentHashMap();

    public void fireEvent(String event, EventPayload payload) {
        if (globalListeners.size() > 0) {
            CompletePayload completePayload = new CompletePayload();
            completePayload.setIsEvent(false);
            completePayload.setChannel(event);
            completePayload.setOriginalPayload(payload);
            globalListeners.forEach(gl -> gl.call(completePayload));
        }

        if (events.containsKey(event)) {
            for (Iterator<Listener> it = events.get(event).iterator(); it.hasNext(); ) {
                Listener l = it.next();
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
            for (Iterator<Listener> it = systemEvents.get(event).iterator(); it.hasNext(); ) {
                Listener l = it.next();
                l.call(payload);
            }
        }
    }

    public EventHandler on(GlobalListener globalListener) {
        globalListeners.add(globalListener);
        return this;
    }

    public EventHandler on(String event, Listener listener) {
        ConcurrentLinkedQueue<Listener> callbacks = events.get(event);
        if (callbacks == null) {
            callbacks = new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue<Listener> tempCallbacks = events.putIfAbsent(event, callbacks);
            if (tempCallbacks != null) {
                callbacks = tempCallbacks;
            }
        }
        callbacks.add(listener);
        return this;
    }

    public EventHandler on(Event e, Listener listener) {
        ConcurrentLinkedQueue<Listener> callbacks = systemEvents.get(e);
        if (callbacks == null) {
            callbacks = new ConcurrentLinkedQueue();
            ConcurrentLinkedQueue<Listener> tempCallbacks = systemEvents.putIfAbsent(e, callbacks);
            if (tempCallbacks != null) {
                callbacks = tempCallbacks;
            }
        }
        callbacks.add(listener);
        return this;
    }

}
