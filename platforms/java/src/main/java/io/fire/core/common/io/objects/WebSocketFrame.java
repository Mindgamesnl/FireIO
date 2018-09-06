package io.fire.core.common.io.objects;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WebSocketFrame {
    private byte opcode;
    private boolean fin;
    private byte payload[];
}
