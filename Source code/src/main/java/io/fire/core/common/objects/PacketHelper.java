package io.fire.core.common.objects;

import io.fire.core.common.eventmanager.EventHandler;
import io.fire.core.common.interfaces.Packet;

import java.io.*;

public class PacketHelper {

    public PacketHelper(EventHandler eventHandler) {

    }

    public Packet fromString(byte[] s) {
        ByteArrayInputStream bis = new ByteArrayInputStream(s);
        ObjectInput in = null;
        Packet finalOut = null;
        try {
            in = new ObjectInputStream(bis);
            finalOut = (Packet) in.readObject();

        } catch (IOException e) {
            System.err.println("UNABLE TO DECODE PACKET!!!");
            System.err.println("Error: ");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("UNABLE TO DECODE PACKET!!!");
            System.err.println("Error: ");
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return finalOut;
    }

    public byte[] toString(Packet o) {
        //create buffer and output streams
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        byte[] yourBytes = new byte[0];
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            yourBytes = bos.toByteArray();

        } catch (IOException e) {
            System.err.println("UNABLE TO DECODE PACKET!!!");
            System.err.println("Error: ");
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
        return yourBytes;
    }


}
