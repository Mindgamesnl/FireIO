package io.fire.core.common.eventmanager;

import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.interfaces.CompletePayload;
import io.fire.core.common.eventmanager.interfaces.EventPayload;
import io.fire.core.common.eventmanager.interfaces.GlobalListener;
import io.fire.core.common.eventmanager.interfaces.Listener;

import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@NoArgsConstructor
public class EventHandler {

    //storage of events and what to do with them
    private Map<String, ConcurrentLinkedQueue<Listener>> events = new ConcurrentHashMap();
    private Map<Event, ConcurrentLinkedQueue<Listener>> systemEvents = new ConcurrentHashMap();

    //thread pool service
    ExecutorService pool = Executors.newFixedThreadPool(1);

    //debug listeners
    private List<GlobalListener> globalListeners = new ArrayList<>();

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
            pool.execute(() -> {
                for (Listener l : events.get(event)) {
                    //call listener
                    l.call(payload);
                }
            });
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
            pool.execute(() -> {
                for (Listener l : systemEvents.get(event)) {
                    //call listener
                    l.call(payload);
                }
            });
        }
    }

    public void setPoolSize(int size) {
        pool.shutdown();
        Executors.newFixedThreadPool(size);
    }

    public void shutdown() {
        pool.shutdown();
    }

    public EventHandler on(GlobalListener globalListener) {
        globalListeners.add(globalListener);
        return this;
    }

    public EventHandler on(String event, Listener listener) {
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

    public EventHandler on(Event e, Listener listener) {
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
