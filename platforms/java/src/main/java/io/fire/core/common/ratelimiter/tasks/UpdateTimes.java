package io.fire.core.common.ratelimiter.tasks;

import io.fire.core.common.ratelimiter.RateLimit;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.TimerTask;

@AllArgsConstructor
public class UpdateTimes extends TimerTask {

    private RateLimit parent;
    private int timeOut;

    @Override
    public void run() {
        Instant now = Instant.now();
        parent.getTimedQueue().forEach((key, value) -> {
            if (Duration.between(value.getUpdatedAt(), now).getSeconds() >= timeOut) parent.getTimedQueue().remove(key);
        });
    }
}
