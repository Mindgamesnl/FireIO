package io.fire.core.common.ratelimiter.objects;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateValue {

    private Instant updatedAt;
    private int streak;

    public void trigger() {
        streak++;
    }

}
