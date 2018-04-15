import io.fire.core.common.interfaces.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CookieJar extends Packet {
    private int amount = 0;
    private String type;
}
