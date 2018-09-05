package io.fire.core.common.io;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WebSocketTransaction {

    private List<String> data;
    private WebSocketStatus status;

}
