package io.fire.core.common.objects;

import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;

@NoArgsConstructor
public class BufferManager {

    private Queue<Byte> byteBuffer = new ConcurrentLinkedDeque<Byte>() {};
    private Byte[] portion = new Byte[4];
    private int portionIndex = 0;
    private boolean isNegative = false;
    @Setter
    private Consumer<byte[]> onInput = (p) -> {
    };

    public void handleData(byte[] input) {
        for (byte a : input) {
            switch (((char) a)) {
                case 's':
                    updatePortion();
                    byte[] ret = new byte[byteBuffer.toArray().length];
                    Iterator<Byte> iterator = byteBuffer.iterator();
                    for (int i = 0; i < ret.length; i++) ret[i] = iterator.next().byteValue();
                    onInput.accept(ret);
                    byteBuffer.clear();
                    break;
                case ',':
                    updatePortion();
                    break;
                case '-':
                    isNegative = true;
                    break;
                default:
                    portion[portionIndex] = Byte.decode((char) a + "");
                    portionIndex++;
                    break;
            }
        }
    }

    private void updatePortion() {
        String bytePart = "";
        if (isNegative) bytePart += "-";
        for (Byte part : portion) if (part != null) bytePart += part;
        byteBuffer.add(Byte.decode(bytePart));
        portion = new Byte[4];
        portionIndex = 0;
        isNegative = false;
    }

}
