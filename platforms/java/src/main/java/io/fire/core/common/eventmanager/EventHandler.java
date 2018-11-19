package io.fire.core.common.eventmanager;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@NoArgsConstructor
public class EventHandler {

    //storage of events and what to do with them
    private Map<String, ConcurrentLinkedQueue<Consumer<EventPayload>>> events = new ConcurrentHashMap();
    private Map<Event, ConcurrentLinkedQueue<Consumer<EventPayload>>> systemEvents = new ConcurrentHashMap();


    /**
     * Trigger a event on a channel listener
     *
     * @param event
     * @param payload
     */
    public void fireEvent(String event, EventPayload payload) {
        //fire channel based event!
        //check if it has any api listeners
        if (events.containsKey(event)) {
            //loop for all listeners
            for (Consumer<EventPayload> cons : events.get(event)) {
                //call listener
                cons.accept(payload);
            }
        }
    }


    /**
     * Fire an API event and trigger the listeners
     *
     * @param event
     * @param payload
     */
    public void fireEvent(Event event, EventPayload payload) {
        //fire channel based event!

        //check if it has any api listeners
        if (systemEvents.containsKey(event)) {
            //loop for all listeners in its own pool

            for (Consumer<EventPayload> cons : systemEvents.get(event)) {
                //call listener
                if (cons != null) cons.accept(payload);
            }
        }
    }


    /**
     * Register a channel listener
     *
     * @param event
     * @param listener
     * @return
     */
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


    /**
     * Register a event listener
     *
     * @param e
     * @param listener
     * @return
     */
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
