package io.fire.core.common.interfaces;

import io.fire.core.common.eventmanager.interfaces.EventPayload;

import java.io.Serializable;

public abstract class Packet implements EventPayload, Serializable {

    //abstract packet class!
    //works as event payload and makes it serializable
    //commonly used in server and client

}
