package io.fire.core.common.eventmanager;


import io.fire.core.common.eventmanager.enums.Event;
import io.fire.core.common.eventmanager.enums.EventPriority;
import io.fire.core.common.eventmanager.executors.EventExecutor;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.enums.InstanceSide;
import io.fire.core.server.modules.client.superclasses.Client;

import java.util.*;
import java.util.stream.Collectors;

public class EventHandler {

    private Map<String, List<EventExecutor>> executorMap = new HashMap<>();
    private InstanceSide instanceSide;


    /**
     * Setup the EventHandler by registering the instance side
     *
     * @param instanceSide
     */
    public EventHandler(InstanceSide instanceSide) {
        this.instanceSide = instanceSide;
    }


    /**
     * Register a class (Packet) as an event, along with a channel and priority
     *
     * @param event
     * @param channel
     * @param priority
     * @param <E>
     * @return
     */
    public <E extends Packet> EventExecutor<E> registerEvent(Class<E> event, String channel, EventPriority priority) {
        //check if it exists, if not then add it to chan
        if (!executorMap.containsKey(event.getName())) executorMap.put(event.getName(), new ArrayList<>());

        //setup executor
        EventExecutor<E> executor = new EventExecutor<>(channel, priority);

        //register and return executor
        executorMap.get(event.getName()).add(executor);

        return executor;
    }


    /**
     * Register a text channel, with the channel name and the priority
     *
     * @param channel
     * @param priority
     * @param <E>
     * @return
     */
    public <E extends String> EventExecutor<E> registerTextChannel(String channel, EventPriority priority) {
        //check if it exists, if not then add it to chan
        if (!executorMap.containsKey("text")) executorMap.put("text", new ArrayList<>());

        //setup executor
        EventExecutor<E> executor = new EventExecutor<>(channel, priority);

        //register and return executor
        executorMap.get("text").add(executor);

        return executor;
    }


    /**
     * Trigger a text channel, by the client (invoker), the channel and the string (payload)
     *
     * @param client
     * @param channel
     * @param string
     */
    public void triggerTextChannel(Client client, String channel, String string) {
        //check if it exists, if not, cancel since there are no handlers, so why do anything
        if (!executorMap.containsKey("text")) return;

        //get all executors, and pass them the payload
        executorMap.get("text")
                .stream()
                .filter(eventExecutor -> eventExecutor.getChannel().equals(channel))
                .sorted(Comparator.comparing(eventExecutor -> eventExecutor.getEventPriority().getLevel()))
                .collect(Collectors.toList())
                .forEach(eventExecutor -> eventExecutor.run(client, string));
    }


    /**
     * Trigger a packet listener, requires the client (invoker), the packet instance (payload and data) and the channel
     *
     * @param client
     * @param packet
     * @param channel
     */
    public void triggerPacket(Client client, Packet packet, String channel) {
        //check if it exists, if not, cancel since there are no handlers, so why do anything
        if (!executorMap.containsKey(packet.getClass().getName())) return;

        //get all executors, and pass them the payload
        executorMap.get(packet.getClass().getName())
                .stream()
                .filter(eventExecutor -> eventExecutor.getChannel().equals(channel))
                .sorted(Comparator.comparing(eventExecutor -> eventExecutor.getEventPriority().getLevel()))
                .collect(Collectors.toList())
                .forEach(eventExecutor -> eventExecutor.run(client, packet));
    }


    /**
     * Register an event with string in the callback
     *
     * @param event
     * @return
     */
    public EventExecutor<String> registerEvent(Event event) {
        //check if it exists, if not then add it to chan
        if (!executorMap.containsKey(event.getClass().getName())) executorMap.put(event.getClass().getName(), new ArrayList<>());

        //setup executor
        EventExecutor<String> executor = new EventExecutor<>(event.toString(), EventPriority.NORMAL);

        //register and return executor
        executorMap.get(event.getClass().getName()).add(executor);

        return executor;
    }


    /**
     * Trigger an event, along with a client (invoker) and an optional string (message)
     *
     * @param event
     * @param client
     * @param string
     */
    public void triggerEvent(Event event, Client client, String string) {
        //check if it exists, if not, cancel since there are no handlers, so why do anything
        if (!executorMap.containsKey(event.getClass().getName())) return;

        //get all executors, and pass them the payload
        executorMap.get(event.getClass().getName())
                .stream()
                .filter(eventExecutor -> eventExecutor.getChannel().equals(event.toString()))
                .sorted(Comparator.comparing(eventExecutor -> eventExecutor.getEventPriority().getLevel()))
                .collect(Collectors.toList())
                .forEach(eventExecutor -> eventExecutor.run(client, string));
    }

}
