package io.fire.core.common.io.objects;

import io.fire.core.common.io.enums.IoFrameType;
import lombok.Getter;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

@Getter
public class IoFrame {

    private ByteBuffer buffer;
    private IoFrameType type;

    /**
     * Fire-IO protocol frame.
     *
     * Contains a control byte (first)
     * followed by 1000 content bytes
     *
     * @param type
     * @param content
     * @throws IOException
     */
    public IoFrame(IoFrameType type, byte[] content) throws IOException {
        if (content.length > 1000) throw new IOException("Content length may not be over 1000 bytes.");
        this.type = type;
        buffer = ByteBuffer.allocate(1001);
        buffer.put(type.getContentByte());
        buffer.put(content);
        ((Buffer)buffer).flip();
    }

}
