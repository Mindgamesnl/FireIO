package io.fire.core.common.interfaces;

import io.fire.core.common.extra.Base64;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerialReader {

    protected Packet[] fromString(String s) {
        //read all packets from string
        //crate empty list for packets
        List<Packet> packets = new ArrayList<>();

        //rempve blank lines, only one line of data
        s = s.replaceAll("\n", "").replaceAll("\r", "");

        //is it empty? then return null
        //in case of bad socket input
        if (s.length() == 0) {
            //mothing just go away
            return null;
        }

        //split all packets by the ".end." tag
        //some times, packets get stitched together when they are send in quick completion of one another to save on resources
        String[] serialPackets = s.split(".end.");

        //loop for base64 packets
        for (String i : serialPackets) {
            //strip it of any possible end tags
            i = i.replaceAll(".end.", "");

            //decode
            try {
                //decode with byte array
                byte[] dataBytes = Base64.decode(i, Base64.DEFAULT);
                final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dataBytes);
                final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

                @SuppressWarnings({"unchecked"})
                //cast
                final Packet obj = (Packet) objectInputStream.readObject();

                //close streams
                objectInputStream.close();

                //add packet to list
                packets.add(obj);
            } catch (IOException e) {
                //malformed data!
                //data might have gotten corrupted some how, no way to save it.
                e.printStackTrace();
                System.err.println("UNABLE TO DECODE PACKET!!!");
                System.err.println("Error: ");
                e.printStackTrace();
                System.err.println("DATA: " + s);
            } catch (ClassNotFoundException e) {
                //malformed data!
                //data might have gotten corrupted some how, no way to save it.
                System.err.println("UNABLE TO DECODE PACKET!!!");
                System.err.println("Error: ");
                e.printStackTrace();
                System.err.println("DATA: " + s);
            }
        }

        //convert to array
        Packet[] array = new Packet[packets.size()];
        packets.toArray(array);

        //return all packets
        return array;
    }

    protected String toString(Packet o) {
        //create buffer and output streams
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final ObjectOutputStream objectOutputStream;
        try {
            //stream object to stream
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            //write stream, then close
            objectOutputStream.writeObject(o);
            objectOutputStream.close();
            //convert stream to base64
            String out = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            //add end tag
            out += ".end.";
            //return string
            return out;
        } catch (IOException e) {
            //unable to serialize packet, is it not serializable or does it contains non-serializable components?
            System.err.println("UNABLE TO DECODE PACKET!!!");
            System.err.println("Error: ");
            e.printStackTrace();
            System.err.println("DATA: " + o);
        }
        //return nothing
        return null;
    }

}
