package io.fire.core.client;

import io.fire.core.common.events.EventHandler;
import lombok.Getter;

public class FireIoClient {

    @Getter private int port;
    @Getter private String host;

    @Getter private EventHandler eventHandler = new EventHandler();

    public FireIoClient(String host, int port) {

    }

}
