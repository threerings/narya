//
// $Id: FrameTest.java,v 1.6 2001/12/13 01:35:52 mdb Exp $

package com.threerings.presents.io;

import java.io.*;

import junit.framework.Test;
import junit.framework.TestCase;

public class FrameTest extends TestCase
{
    public FrameTest ()
    {
        super(FrameTest.class.getName());
    }

    public void writeFrames (OutputStream out)
        throws IOException
    {
        FramingOutputStream fout = new FramingOutputStream();
        DataOutputStream dout = new DataOutputStream(fout);

        // create a few frames and write them to the output stream
        dout.writeUTF(STRING1);
        dout.writeUTF(STRING2);
        dout.writeUTF(STRING3);
        fout.writeFrameAndReset(out);

        dout.writeUTF(STRING4);
        dout.writeUTF(STRING5);
        dout.writeUTF(STRING6);
        fout.writeFrameAndReset(out);

        dout.writeUTF(STRING7);
        fout.writeFrameAndReset(out);
    }

    public void readFrames (InputStream in)
        throws IOException
    {
        FramedInputStream fin = new FramedInputStream();
        DataInputStream din = new DataInputStream(fin);

        // read the first frame
        fin.readFrame(in);
        assert("string1", STRING1.equals(din.readUTF()));
        assert("string2", STRING2.equals(din.readUTF()));
        assert("string3", STRING3.equals(din.readUTF()));
        assert("hit eof", fin.read() == -1);

        // read the second frame
        fin.readFrame(in);
        assert("string4", STRING4.equals(din.readUTF()));
        assert("string5", STRING5.equals(din.readUTF()));
        assert("string6", STRING6.equals(din.readUTF()));
        assert("hit eof", fin.read() == -1);

        // read the third frame
        fin.readFrame(in);
        assert("string7", STRING7.equals(din.readUTF()));
        assert("hit eof", fin.read() == -1);
    }

    public void runTest ()
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

    public static Test suite ()
    {
        return new FrameTest();
    }

    protected static final String STRING1 = "This is a test.";
    protected static final String STRING2 = "This is only a test.";
    protected static final String STRING3 =
        "If this were not a test, there would be meaningful data in " +
        "this frame and someone would probably be enjoying themselves.";

    protected static final String STRING4 =
        "Now is the time for all good men to come to the aid of " +
        "their country.";
    protected static final String STRING5 = "Every good boy deserves fudge.";
    protected static final String STRING6 =
        "The quick brown fox jumped over the lazy dog.";

    protected static final String STRING7 = "Third time is the charm.";
}
