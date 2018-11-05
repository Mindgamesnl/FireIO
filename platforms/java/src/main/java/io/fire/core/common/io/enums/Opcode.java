package io.fire.core.common.io.enums;

import lombok.Getter;

public enum  Opcode {

    CONTINUOUS(0), TEXT(1), BINARY(2), PING(9), PONG(10), CLOSING(8), UNKNOWN(-1);

    @Getter private int id;
    Opcode(int i) {
        this.id = i;
    }

    public static Opcode toOpcode( byte opcode ){
        switch(opcode) {
            case 0:
                return Opcode.CONTINUOUS;
            case 1:
                return Opcode.TEXT;
            case 2:
                return Opcode.BINARY;
            case 8:
                return Opcode.CLOSING;
            case 9:
                return Opcode.PING;
            case 10:
                return Opcode.PONG;
            default:
                return UNKNOWN;
        }
    }
}
