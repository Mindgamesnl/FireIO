package io.fire.core.common.body;

import io.fire.core.common.interfaces.RequestBody;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestString implements RequestBody {

    //default payload for requests, can be expended upon by the user via the RequestBody interface
    private String string;

}
