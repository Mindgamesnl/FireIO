package io.fire.core.common.io.frames;

import io.fire.core.common.io.enums.Opcode;
import lombok.Data;
import lombok.Getter;

import java.nio.ByteBuffer;

@Data
public class FrameData {

    @Getter private boolean fin;
    @Getter private Opcode opcode;
    @Getter private ByteBuffer unmaskedpayload;
    @Getter private boolean transferemasked;
    @Getter private boolean rsv1;
    @Getter private boolean rsv2;
    @Getter private boolean rsv3;

    public FrameData(Opcode op) {
        opcode = op;
        unmaskedpayload = ByteBuffer.wrap(new byte[]{});
        fin = true;
        transferemasked = false;
        rsv1 = false;
        rsv2 = false;
        rsv3 = false;
    }

    public ByteBuffer getPayloadData() {
        return unmaskedpayload;
    }

    public void setPayload(ByteBuffer payload) {
        this.unmaskedpayload = payload;
    }

    public void append(FrameData nextframe) {
        ByteBuffer b = nextframe.getPayloadData();
        if (unmaskedpayload == null) {
            unmaskedpayload = ByteBuffer.allocate(b.remaining());
            b.mark();
            unmaskedpayload.put(b);
            b.reset();
        } else {
            b.mark();
            unmaskedpayload.position(unmaskedpayload.limit());
            unmaskedpayload.limit(unmaskedpayload.capacity());

            if (b.remaining() > unmaskedpayload.remaining()) {
                ByteBuffer tmp = ByteBuffer.allocate(b.remaining() + unmaskedpayload.capacity());
                unmaskedpayload.flip();
                tmp.put(unmaskedpayload);
                tmp.put(b);
                unmaskedpayload = tmp;

            } else {
                unmaskedpayload.put(b);
            }
            unmaskedpayload.rewind();
            b.reset();
        }
        fin = nextframe.isFin();
    }

}
