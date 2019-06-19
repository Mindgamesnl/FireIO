package io.fire.core.common.events;


import io.fire.core.common.events.enums.Event;
import io.fire.core.common.events.enums.EventPriority;
import io.fire.core.common.events.executors.EventExecutor;
import io.fire.core.common.io.socket.interfaces.GenericClient;
import io.fire.core.common.io.socket.interfaces.Packet;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
public class EventHandler {

    private Map<String, List<EventExecutor>> executorMap = new HashMap<>();

    public <E extends Packet> EventExecutor<E> registerEvent(Class<E> event, String channel, EventPriority priority) {
        if (!executorMap.containsKey(event.getName())) executorMap.put(event.getName(), new ArrayList<>());
        EventExecutor<E> executor = new EventExecutor<>(channel, priority);
        executorMap.get(event.getName()).add(executor);
        return executor;
    }

    public <E extends String> EventExecutor<E> registerTextChannel(String channel, EventPriority priority) {
        if (!executorMap.containsKey("text")) executorMap.put("text", new ArrayList<>());
        EventExecutor<E> executor = new EventExecutor<>(channel, priority);
        executorMap.get("text").add(executor);
        return executor;
    }

    public void triggerTextChannel(GenericClient client, String channel, String string) {
        if (!executorMap.containsKey("text")) return;
        executorMap.get("text")
                .stream()
                .filter(eventExecutor -> eventExecutor.getChannel().equals(channel))
                .sorted(Comparator.comparing(eventExecutor -> eventExecutor.getEventPriority().getLevel()))
                .collect(Collectors.toList())
                .forEach(eventExecutor -> eventExecutor.run(client, string));
    }

    public void triggerTextChannel(String channel, String string) {
        triggerTextChannel(null, channel, string);
    }

    public void triggerPacket(GenericClient client, String channel, Packet packet) {
        if (!executorMap.containsKey(packet.getClass().getName())) return;
        executorMap.get(packet.getClass().getName())
                .stream()
                .filter(eventExecutor -> eventExecutor.getChannel().equals(channel))
                .sorted(Comparator.comparing(eventExecutor -> eventExecutor.getEventPriority().getLevel()))
                .collect(Collectors.toList())
                .forEach(eventExecutor -> eventExecutor.run(client, packet));
    }

    public void triggerPacket(Packet packet, String channel) {
        triggerPacket(null, channel, packet);
    }

    public EventExecutor<String> registerEvent(Event event) {
        if (!executorMap.containsKey(event.getClass().getName())) executorMap.put(event.getClass().getName(), new ArrayList<>());
        EventExecutor<String> executor = new EventExecutor<>(event.toString(), EventPriority.NORMAL);
        executorMap.get(event.getClass().getName()).add(executor);
        return executor;
    }

    public void triggerEvent(Event event, GenericClient client, String string) {
        if (!executorMap.containsKey(event.getClass().getName())) return;
        executorMap.get(event.getClass().getName())
                .stream()
                .filter(eventExecutor -> eventExecutor.getChannel().equals(event.toString()))
                .sorted(Comparator.comparing((EventExecutor eventExecutor) -> eventExecutor.getEventPriority().getLevel()))
                .collect(Collectors.toList())
                .forEach(eventExecutor -> eventExecutor.run(client, string));
    }

    public void triggerEvent(Event event, String string) {
        triggerEvent(event, null, string);
    }

}

