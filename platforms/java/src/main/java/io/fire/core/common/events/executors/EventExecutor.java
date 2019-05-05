package io.fire.core.common.events.executors;

import io.fire.core.common.events.enums.EventPriority;
import io.fire.core.common.io.socket.interfaces.GenericClient;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EventExecutor<T> {

    @Getter private EventPriority eventPriority;
    @Getter private String channel;

    private Consumer<T> executable;
    private BiConsumer<GenericClient, T> biExecutable;

    public EventExecutor(String channel, EventPriority priority) {
        this.eventPriority = priority;
        this.channel = channel;
    }

    public void run(GenericClient client, T payload) {
        if (this.executable != null) executable.accept(payload);
        if (this.biExecutable != null) biExecutable.accept(client, payload);
    }

    public void onExecute(Consumer<T> executable) {
        this.executable = executable;
    }

    public void onExecute(BiConsumer<GenericClient, T> executable) {
        this.biExecutable = executable;
    }

}
