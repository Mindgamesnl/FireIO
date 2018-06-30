package io.fire.core.common.ratelimiter;

import io.fire.core.common.ratelimiter.objects.RateValue;
import io.fire.core.common.ratelimiter.tasks.UpdateTimes;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimit {

    @Getter private Map<String, RateValue> timedQueue = new ConcurrentHashMap<>();
    private int timeout = 0;
    private int executions = 1;
    private Timer timer = new Timer();

    public RateLimit(int seconds, int executions) {
        this.timeout = seconds;
        this.executions = executions;
        this.timer.schedule(new UpdateTimes(this, timeout), 0, 1000);
    }

    public boolean allowed(String query) {
        if (timedQueue.containsKey(query)) {
            RateValue value = timedQueue.get(query);
            value.setUpdatedAt(Instant.now());
            if (value.getStreak() >= executions) return false;
            value.trigger();
            timedQueue.put(query, value);
            return true;
        } else {
            RateValue rateValue = new RateValue();
            rateValue.setStreak(1);
            rateValue.setUpdatedAt(Instant.now());
            timedQueue.put(query, rateValue);
            return true;
        }
    }

    public void stop() {
        timer.cancel();
    }

}
