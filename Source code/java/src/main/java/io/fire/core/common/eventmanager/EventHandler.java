package io.fire.core.common.eventmanager;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.interfaces.CompletePayload;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.common.eventmanager.interfaces.GlobalListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class EventHandler {

    //storage of events and what to do with them
    private Map<String, ConcurrentLinkedQueue<Consumer<EventPayload>>> events = new ConcurrentHashMap();
    private Map<Event, ConcurrentLinkedQueue<Consumer<EventPayload>>> systemEvents = new ConcurrentHashMap();

    //debug listeners
    private List<GlobalListener> globalListeners = new ArrayList<>();

    public EventHandler() {
    }

    public void fireEvent(String event, EventPayload payload) {
        //fire channel based event!
        //check if it has any listeners for debug global
        if (globalListeners.size() > 0) {
            CompletePayload completePayload = new CompletePayload();
            completePayload.setIsEvent(false);
            completePayload.setChannel(event);
            completePayload.setOriginalPayload(payload);
            globalListeners.forEach(gl -> gl.call(completePayload));
        }

        //check if it has any api listeners
        if (events.containsKey(event)) {
            //loop for all listeners
            for (Consumer<EventPayload> cons : events.get(event)) {
                //call listener
                cons.accept(payload);
            }
        }
    }

    public void fireEvent(Event event, EventPayload payload) {
        //fire channel based event!
        //check if it has any listeners for debug global
        if (globalListeners.size() > 0) {
            CompletePayload completePayload = new CompletePayload();
            completePayload.setIsEvent(true);
            completePayload.setEvent(event);
            completePayload.setChannel(event.toString());
            completePayload.setOriginalPayload(payload);
            globalListeners.forEach(gl -> gl.call(completePayload));
        }

        //check if it has any api listeners
        if (systemEvents.containsKey(event)) {
            //loop for all listeners in its own pool

            for (Consumer<EventPayload> cons : systemEvents.get(event)) {
                //call listener
                if (cons != null) cons.accept(payload);
            }
        }
    }

    public EventHandler on(GlobalListener globalListener) {
        globalListeners.add(globalListener);
        return this;
    }

    public EventHandler on(String event, Consumer<EventPayload> listener) {
        //register channel listener
        //check if there already exists a que for this listener
        //it needs to be a que so if there are multiple listeners for one event they will all be triggered in order
        ConcurrentLinkedQueue callbacks = events.get(event);
        //check if it returned null
        if (callbacks == null) {
            //create new linked list for listeners
            callbacks = new ConcurrentLinkedQueue();
            //save with channel as key and list as value
            ConcurrentLinkedQueue tempCallbacks = events.putIfAbsent(event, callbacks);
            if (tempCallbacks != null) {
                callbacks = tempCallbacks;
            }
        }
        //add listener to list
        callbacks.add(listener);
        return this;
    }

    public EventHandler on(Event e, Consumer<EventPayload> listener) {
        //register channel listener
        //check if there already exists a que for this listener
        //it needs to be a que so if there are multiple listeners for one event they will all be triggered in order
        ConcurrentLinkedQueue callbacks = systemEvents.get(e);
        //check if it returned null
        if (callbacks == null) {
            //create new linked list for listeners
            callbacks = new ConcurrentLinkedQueue();
            //save with channel as key and list as value
            ConcurrentLinkedQueue tempCallbacks = systemEvents.putIfAbsent(e, callbacks);
            if (tempCallbacks != null) {
                callbacks = tempCallbacks;
            }
        }
        //add listener to list
        callbacks.add(listener);
        return this;
    }

}
