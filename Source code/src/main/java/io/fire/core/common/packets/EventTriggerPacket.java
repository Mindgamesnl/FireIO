package io.fire.core.common.packets;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventTriggerPacket implements Serializable {

    private String event;
    private String data;

}
