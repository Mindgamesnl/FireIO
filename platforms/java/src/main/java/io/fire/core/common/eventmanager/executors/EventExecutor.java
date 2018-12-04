package io.fire.core.common.eventmanager.executors;

import io.fire.core.common.eventmanager.enums.EventPriority;
import io.fire.core.server.modules.client.superclasses.Client;
import lombok.Getter;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EventExecutor<T> {

    @Getter private EventPriority eventPriority;
    @Getter private String channel;

    private Consumer<T> executable;
    private BiConsumer<Client, T> biExecutable;

    public EventExecutor(String channel, EventPriority priority) {
        this.eventPriority = priority;
        this.channel = channel;
    }

    public void run(Client client, T payload) {
        if (this.executable != null) executable.accept(payload);
        if (this.biExecutable != null) biExecutable.accept(client, payload);
    }

    public void onExecute(Consumer<T> executable) {
        this.executable = executable;
    }

    public void onExecute(BiConsumer<Client, T> executable) {
        this.biExecutable = executable;
    }

}
