//
// $Id: FrameTest.java,v 1.3 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.io.test;

import java.io.*;
import com.threerings.presents.io.*;

public class FrameTest
{
    public static void writeFrames (OutputStream out)
        throws IOException
    {
        FramingOutputStream fout = new FramingOutputStream();
        DataOutputStream dout = new DataOutputStream(fout);

        // create a few frames and write them to the output stream
        dout.writeUTF("This is a test.");
        dout.writeUTF("This is only a test.");
        dout.writeUTF("If this were not a test, there would be " +
                      "meaningful data in this frame and someone " +
                      "would probably be enjoying themselves.");
        fout.writeFrameAndReset(out);

        dout.writeUTF("Now is the time for all good men to come to the " +
                      "aid of their country.");
        dout.writeUTF("Every good boy deserves fudge.");
        dout.writeUTF("The quick brown fox jumped over the lazy cow.");
        fout.writeFrameAndReset(out);

        dout.writeUTF("Third time is the charm.");
        fout.writeFrameAndReset(out);
    }

    public static void readFrames (InputStream in)
        throws IOException
    {
        FramedInputStream fin = new FramedInputStream();
        DataInputStream din = new DataInputStream(fin);

        // read the first frame
        fin.readFrame(in);
        System.out.println(din.readUTF());
        System.out.println(din.readUTF());
        System.out.println(din.readUTF());
        System.out.println("This should be -1: " + fin.read());

        // read the second frame
        fin.readFrame(in);
        System.out.println(din.readUTF());
        System.out.println(din.readUTF());
        System.out.println(din.readUTF());
        System.out.println("This should be -1: " + fin.read());

        // read the third frame
        fin.readFrame(in);
        System.out.println(din.readUTF());
        System.out.println("This should be -1: " + fin.read());
    }

    public static void main (String[] args)
    {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            writeFrames(bout);

            byte[] data = bout.toByteArray();
            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            readFrames(bin);

        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
}
