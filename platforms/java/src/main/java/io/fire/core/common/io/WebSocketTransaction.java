package io.fire.core.common.io;

import io.fire.core.common.io.enums.WebSocketStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WebSocketTransaction {

    private String data;
    private WebSocketStatus status;

}
