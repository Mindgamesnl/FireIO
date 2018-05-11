package io.fire.core.common.eventmanager.interfaces;

public interface Listener {

    //listener, main callback interface for the event system

    void call(EventPayload eventPayload);

}
