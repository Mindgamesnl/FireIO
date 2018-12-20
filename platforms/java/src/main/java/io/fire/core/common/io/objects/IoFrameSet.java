package io.fire.core.common.io.objects;

import io.fire.core.TempDebug;
import io.fire.core.common.interfaces.Packet;
import io.fire.core.common.io.IoManager;
import io.fire.core.common.io.enums.InstanceSide;
import io.fire.core.common.io.enums.IoFrameType;
import lombok.Getter;

import javax.activation.UnsupportedDataTypeException;
import java.io.*;
import java.time.Instant;
import java.util.LinkedList;

public class IoFrameSet {

    private boolean isReading;
    private byte[] content = new byte[0];

    @Getter private LinkedList<IoFrame> frames = new LinkedList<>();
    @Getter private boolean isFinished = false;
    @Getter private Packet payload;
    @Getter private IoFrameType firstType = IoFrameType.UNKNOWN;
    private boolean startedReading = false;
    private IoManager ioManager;

    /**
     * Generate a Io-Frame-Set based on a Packet object
     *
     * @param input
     * @throws IOException
     */
    public IoFrameSet(Packet input) throws IOException {
        this.isReading = false;
        byte[] bytesOut = new byte[0];
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(input);
            out.flush();
            bytesOut = bos.toByteArray();
        } catch (IOException e) {
            System.err.println("Failed to serialize packet.");
            e.printStackTrace();
        }

        if (bytesOut.length > 1000) {
            //to big of an array, spit it in multiple frames
            int numOfChunks = (int)Math.ceil((double)bytesOut.length / 1000);

            for(int i = 0; i < numOfChunks; ++i) {
                int start = i * 1000;
                int length = Math.min(bytesOut.length - start, 1000);

                byte[] chunk = new byte[length];
                System.arraycopy(bytesOut, start, chunk, 0, length);

                //create frame
                IoFrameType currentFrame = null;
                if (i == 0) currentFrame = IoFrameType.START;
                if (i == (numOfChunks -1)) currentFrame = IoFrameType.FINISH;
                if (currentFrame == null) currentFrame = IoFrameType.CONTINUE;

                frames.add(new IoFrame(currentFrame, chunk));
            }
        } else {
            //just fuck it over the socket
            frames.add(new IoFrame(IoFrameType.SINGLE, bytesOut));
        }
    }


    /**
     * Generate a FrameSet with a specific status code
     *
     * @param type
     */
    public IoFrameSet(IoFrameType type) {
        if (!(type == IoFrameType.CONFIRM_PACKET || type == IoFrameType.PING_PACKET)) throw new IllegalArgumentException("Can not create packet based on " + type);
        try {
            frames.add(new IoFrame(type, new byte[1000]));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Generate a empty FrameSet, used for reading
     */
    public IoFrameSet() {
        this.isReading = true;
    }


    /**
     * Generate frame set with client handler, so we can handle pings
     *
     * @param ioManager
     */
    public IoFrameSet(IoManager ioManager) {
        this.isReading = true;
        this.ioManager = ioManager;
    }


    /**
     * Utility function to check the validity of a byte array
     * also mark for reading
     *
     * @param in
     * @return
     */
    private boolean isZero(byte[] in) {
        for (byte b : in) if (b != 0) return false;
        return true;
    }


    /**
     * Read a packet.
     *
     * Converts a Packet with a size of 1001 to its matching status code and content
     *
     * if it is marked as completed, de-serialize it as an object
     *
     * @param packet
     * @throws IOException
     */
    public void readInput(byte[] packet) throws IOException {
        if (!isReading) throw new IllegalStateException("Input readers may not receive data when it is writing a packet");
        IoFrameType receivedType = IoFrameType.fromBytes(packet);

        if (!startedReading) {
            TempDebug.print("Starting a new frame");
            if (receivedType == IoFrameType.UNKNOWN && !isZero(packet)) throw new UnsupportedDataTypeException("Could not accept packet type of unknown value " + packet[0]);

            if (receivedType == IoFrameType.CONFIRM_PACKET) {
                isFinished = true;
                firstType = IoFrameType.CONFIRM_PACKET;
                return;
            }

            if (receivedType == IoFrameType.PING_PACKET) {
                if (ioManager.getSide() == InstanceSide.CLIENT) {
                    ioManager.getClient().getSocketModule().getConnection().setLastPing(Instant.now());
                }
                return;
            }
        } else {
            TempDebug.print("Expanding a new frame");
        }

        startedReading = true;

        if (receivedType == IoFrameType.SINGLE) {
            content = new byte[1000];
            System.arraycopy(packet, 1, content,0 , packet.length - 1);
            isFinished = true;
        } else {
            byte[] oldContent = content;
            this.content = new byte[oldContent.length + 1000];
            //init the new content with start of old
            System.arraycopy(oldContent, 0, this.content,0 , oldContent.length);
            //append newly received packet content
            System.arraycopy(packet, 1, content, oldContent.length, packet.length - 1);
            //mark as finished if received
            isFinished = receivedType == IoFrameType.FINISH;
        }

        finish();
    }

    private void finish() throws IOException {
        if (isFinished) {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
            try (ObjectInput in = new ObjectInputStream(inputStream)) {
                payload = (Packet) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                throw new IOException("The buffered input does not corrospond with a java class, either it is corrupted or you class is not included. e:"+e.getClass().getSimpleName());
            }
        }
    }

}
