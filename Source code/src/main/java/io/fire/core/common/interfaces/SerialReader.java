package io.fire.core.common.interfaces;

import io.fire.core.common.android.Base64;

import java.io.*;

public class SerialReader {

    protected Packet[] fromString(String s) {
        byte index = 0;
        Packet[] packets = new Packet[2];

        s = s.replaceAll("\n", "").replaceAll("\r", "");

        if (s.length() == 0) {
            //mothing just go away
            return null;
        }

        //to save otherwise dropped packets
        String nextchar = s.substring(s.indexOf("=") + 1);

        if (nextchar.startsWith("=")) {
            nextchar = s.substring(s.indexOf("=") + 2);
        }

        if (nextchar != null && !nextchar.equals(s)) {
            if ((nextchar.equals("") && !nextchar.equals("=")) || nextchar.length() > 2) {
                s = s.replaceFirst(nextchar, "");
                if (!(s.length() < 3)) {
                    Packet[] pa = fromString(nextchar);
                    if (pa != null && pa.length != 0) {
                        packets[0] = pa[0];
                        index++;
                    }
                }
            }
        }

        //normal packet
        try {
            byte[] dataBytes = Base64.decode(s, Base64.DEFAULT);
            final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dataBytes);
            final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

            @SuppressWarnings({"unchecked"})
            final Packet obj = (Packet) objectInputStream.readObject();

            objectInputStream.close();
            packets[index] = obj;
            return packets;
        } catch (IOException e) {
            e.printStackTrace();
            throw new Error(e);
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }

    protected String toString(Packet o) throws IOException {
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
            return Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

}
