package io.fire.core.client.modules.socket.task;

import io.fire.core.client.modules.socket.handlers.AsyncConnectionHandler;
import io.fire.core.common.eventmanager.enums.Event;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.TimerTask;

@AllArgsConstructor
public class PingCheckTask extends TimerTask {

    private AsyncConnectionHandler connectionHandler;

    /**
     * Repeating task, checks the time of the last ping received to check if the server is still keeping up or if the connection died
     */
    @Override
    public void run() {
        if (connectionHandler.getIsSetup() && !connectionHandler.getIsDead()) {
            //check if time is at least 5 seconds old
            if (Duration.between(connectionHandler.getLastPing(), Instant.now()).getSeconds() >= 5) {
                System.out.println("[Fire-IO] Did not receive ping from server in >5 seconds, assuming that the connection is dead.");
                connectionHandler.getClient().getEventHandler().triggerEvent(Event.DISCONNECT, null, "Did not receive ping from server in >5 seconds, assuming that the connection is dead.");
                connectionHandler.getClient().getEventHandler().triggerEvent(Event.TIMED_OUT, null, "Did not receive ping from server in >5 seconds, assuming that the connection is dead.");
                connectionHandler.close();
            }
        }
    }
}
