package io.fire.core.common.objects;

import io.fire.core.common.interfaces.RequestBody;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestString implements RequestBody {

    private String string;

}
