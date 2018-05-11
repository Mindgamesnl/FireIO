package io.fire.core.common.interfaces;

import io.fire.core.common.extra.Base64;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerialReader {

    protected Packet[] fromString(String s) {
        List<Packet> packets = new ArrayList<>();


        s = s.replaceAll("\n", "").replaceAll("\r", "");

        if (s.length() == 0) {
            //mothing just go away
            return null;
        }

        String[] serialPackets = s.split(".end.");

        for (String i : serialPackets) {
            i = i.replaceAll(".end.", "");

            //normal packet
            try {
                byte[] dataBytes = Base64.decode(i, Base64.DEFAULT);
                final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dataBytes);
                final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

                @SuppressWarnings({"unchecked"})
                final Packet obj = (Packet) objectInputStream.readObject();

                objectInputStream.close();
                packets.add(obj);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("UNABLE TO DECODE PACKET!!!");
                System.err.println("Error: ");
                e.printStackTrace();
                System.err.println("DATA: " + s);
            } catch (ClassNotFoundException e) {
                System.err.println("UNABLE TO DECODE PACKET!!!");
                System.err.println("Error: ");
                e.printStackTrace();
                System.err.println("DATA: " + s);
            }
        }

        Packet[] array = new Packet[packets.size()];
        packets.toArray(array);

        return array;
    }

    protected String toString(Packet o) {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
            String out = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            out += ".end.";
            return out;
        } catch (IOException e) {
            System.err.println("UNABLE TO DECODE PACKET!!!");
            System.err.println("Error: ");
            e.printStackTrace();
            System.err.println("DATA: " + o);
        }
        return null;
    }

}
