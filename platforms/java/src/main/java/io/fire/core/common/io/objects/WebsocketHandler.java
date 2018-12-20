package io.fire.core.common.io.objects;

import io.fire.core.common.io.frames.FrameData;
import lombok.NoArgsConstructor;

import java.nio.Buffer;
import java.nio.ByteBuffer;

@NoArgsConstructor
public class WebsocketHandler {

    public byte[] toByteArray(long val, int bytecount) {
        byte[] buffer = new byte[bytecount];
        int highest = 8 * bytecount - 8;
        for (int i = 0; i < bytecount; i++) {
            buffer[i] = (byte) (val >>> (highest - 8 * i));
        }
        return buffer;
    }

    public ByteBuffer parseData(FrameData framedata) {
        ByteBuffer mes = framedata.getPayloadData();
        int byteSize = mes.remaining() <= 125 ? 1 : mes.remaining() <= 65535 ? 2 : 8;
        ByteBuffer buf = ByteBuffer.allocate(1 + (byteSize > 1 ? byteSize + 1 : byteSize) + 0 + mes.remaining());
        byte opt = (byte) framedata.getOpcode().getId();
        byte one = (byte) (framedata.isFin() ? -128 : 0);
        one |= opt;
        buf.put(one);
        byte[] payload = toByteArray(mes.remaining(), byteSize);
        assert (payload.length == byteSize);
        if (byteSize == 1) {
            buf.put((byte) (payload[0] | 0));
        } else if (byteSize == 2) {
            buf.put((byte) ((byte) 126 | 0));
            buf.put(payload);
        } else if (byteSize == 8) {
            buf.put((byte) ((byte) 127 | 0));
            buf.put(payload);
        }
        buf.put(mes);
        ((Buffer)mes).flip();
        assert (buf.remaining() == 0) : buf.remaining();
        ((Buffer)buf).flip();
        return buf;
    }

    public WebSocketFrame parseEncodedFrame(byte[] raw) {
        ByteBuffer buf = ByteBuffer.wrap(raw);
        WebSocketFrame frame = new WebSocketFrame();
        byte b = buf.get();
        frame.setFin(((b & 0x80) != 0));
        frame.setOpcode((byte) (b & 0x0F));

        b = buf.get();
        boolean masked = ((b & 0x80) != 0);
        int payloadLength = (byte) (0x7F & b);
        int byteCount = 0;
        if (payloadLength == 0x7F) byteCount = 8;
        else if (payloadLength == 0x7E) byteCount = 2;

        while (--byteCount > 0) {
            b = buf.get();
            payloadLength |= (b & 0xFF) << (8 * byteCount);
        }

        byte maskingKey[] = null;
        if (masked) {
            maskingKey = new byte[4];
            buf.get(maskingKey, 0, 4);
        }

        frame.setPayload(new byte[payloadLength]);
        buf.get(frame.getPayload(), 0, payloadLength);

        if (masked) for (int i = 0; i < frame.getPayload().length; i++) frame.getPayload()[i] ^= maskingKey[i % 4];
        return frame;
    }

}
