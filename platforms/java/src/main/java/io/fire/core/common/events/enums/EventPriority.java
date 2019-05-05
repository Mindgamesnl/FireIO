package io.fire.core.common.events.enums;

import lombok.Getter;

public enum EventPriority {

    LOWEST(-2),
    LOW(-1),
    NORMAL(0),
    HIGH(1),
    HIGHEST(2);

    @Getter
    private int level;

    EventPriority(int level) {
        this.level = level;
    }

}
