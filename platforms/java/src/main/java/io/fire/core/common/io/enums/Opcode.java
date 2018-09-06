package io.fire.core.common.io.enums;

import lombok.Getter;

public enum  Opcode {

    CONTINUOUS(0), TEXT(1), BINARY(2), PING(9), PONG(10), CLOSING(8);

    @Getter private int id;
    Opcode(int i) {
        this.id = i;
    }
}
