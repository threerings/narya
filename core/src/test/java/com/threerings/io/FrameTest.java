//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.channels.spi.SelectorProvider;

import org.junit.Test;
import static org.junit.Assert.*;

public class FrameTest
{
    public void writeFrames (WritableByteChannel out)
        throws IOException
    {
        FramingOutputStream fout = new FramingOutputStream();
        DataOutputStream dout = new DataOutputStream(fout);

        // create a few frames and write them to the output stream
        dout.writeUTF(STRING1);
        dout.writeUTF(STRING2);
        dout.writeUTF(STRING3);
        out.write(fout.frameAndReturnBuffer());
        fout.resetFrame();

        dout.writeUTF(STRING4);
        dout.writeUTF(STRING5);
        dout.writeUTF(STRING6);
        out.write(fout.frameAndReturnBuffer());
        fout.resetFrame();

        dout.writeUTF(STRING7);
        out.write(fout.frameAndReturnBuffer());
        fout.resetFrame();
    }

    public void readFrames (ReadableByteChannel in)
        throws IOException
    {
        FramedInputStream fin = new FramedInputStream();
        DataInputStream din = new DataInputStream(fin);

        // read the first frame
        fin.readFrame(in);
        assertTrue("string1", STRING1.equals(din.readUTF()));
        assertTrue("string2", STRING2.equals(din.readUTF()));
        assertTrue("string3", STRING3.equals(din.readUTF()));
        assertTrue("hit eof", fin.read() == -1);

        // read the second frame
        fin.readFrame(in);
        assertTrue("string4", STRING4.equals(din.readUTF()));
        assertTrue("string5", STRING5.equals(din.readUTF()));
        assertTrue("string6", STRING6.equals(din.readUTF()));
        assertTrue("hit eof", fin.read() == -1);

        // read the third frame
        fin.readFrame(in);
        assertTrue("string7", STRING7.equals(din.readUTF()));
        assertTrue("hit eof", fin.read() == -1);
    }

    // @Test
    public void testReadWriteFrames () throws IOException
    {
        Pipe pipe = SelectorProvider.provider().openPipe();
        writeFrames(pipe.sink());
        readFrames(pipe.source());
    }

    @Test
    public void testInvariantFailure () throws Exception {
        final int[] FRAME_COUNTS = new int[] {
            14349, 14054, 15574, 575, 15497, 5417, 3732, 13746, 3043, 3829, 15862, 2285, 4242,
            4903, 16361, 14213, 7981, 4591, 12263, 16268, 15296, 11822, 2713, 4353, 15013, 9810,
            3015, 189, 7988, 6347, 5859, 13433, 3551, 13795, 5164, 7821, 2383, 1513, 16383, 2106,
            7376, 8168
        };

        for (int cc = 0; cc < 50; cc += 1) {
            final Pipe pipe = SelectorProvider.provider().openPipe();
            final WritableByteChannel out = pipe.sink();
            final ReadableByteChannel in = pipe.source();

            // write out the frames of the specified sizes
            Thread writer = new Thread() {
                public void run () {
                    try {
                        FramingOutputStream fout = new FramingOutputStream();
                        DataOutputStream dout = new DataOutputStream(fout);

                        for (int count : FRAME_COUNTS) {
                            for (int ii = 0; ii < count; ii++) dout.writeInt(ii);
                            ByteBuffer frame = fout.frameAndReturnBuffer();
                            int wrote = 0, size = frame.remaining(), window = 128;
                            while (wrote < size) {
                                // write out the buffer in small chunks so we trigger the failure in
                                // the reader
                                frame.limit(Math.min(window, size));
                                wrote += out.write(frame);
                                window += 128;
                            }
                            fout.resetFrame();
                            // System.out.println("Sent frame @ " + count);
                        }

                    } catch (IOException ioe) {
                        fail(ioe.getMessage());
                    }
                }
            };
            writer.start();

            // read them back in and check their contents
            FramedInputStream fin = new FramedInputStream();
            DataInputStream din = new DataInputStream(fin);
            for (int ff = 0; ff < FRAME_COUNTS.length; ff += 1) {
                try {
                    while (!fin.readFrame(in)) {} // loop!
                    int count = fin.available() / 4;
                    for (int ii = 0; ii < count; ii++) assertEquals(ii, din.readInt());
                    // System.out.println("Read frame " + ff + " @ " + count);
                } catch (IllegalArgumentException iae) {
                    System.err.println("Invariant violation reading frame " + ff + ": " + iae);
                    throw iae;
                }
            }

            writer.join();
            in.close();
            out.close();
        }
    }

    // this soak test was used to generate a series of frame sizes that triggered the nasty bug
    // that is isolated and tested by the above testInvariantFailure test; let's hope we go another
    // 17 years before we have to use the soak test again

    // @Test
    public void testManyRandomFrames () throws Exception {
        final int PAIRS = 102400;

        for (int pp = 0; pp < PAIRS; pp++) {
            if (pp % 128 == 0) System.out.println("Starting reader/writer pair " + pp);

            final Pipe pipe = SelectorProvider.provider().openPipe();
            final WritableByteChannel out = pipe.sink();
            final ReadableByteChannel in = pipe.source();
            final int FRAMES = 100;
            final List<Integer> frameSizes = new ArrayList<Integer>();

            // write out a series of randomly sized frames
            Thread writer = new Thread() {
                public void run () {
                    try {
                        Random rando = new Random();
                        FramingOutputStream fout = new FramingOutputStream();
                        DataOutputStream dout = new DataOutputStream(fout);

                        for (int ff = 0; ff < FRAMES; ff++) {
                            int count = rando.nextInt(16*1024);
                            frameSizes.add(count);
                            for (int ii = 0; ii < count; ii++) dout.writeInt(ii);
                            ByteBuffer frame = fout.frameAndReturnBuffer();
                            int wrote = 0;
                            while (wrote < frame.limit()) wrote += out.write(frame);
                            fout.resetFrame();
                            // if (ff % 1000 == 0) System.out.println("Sent frame " + ff);
                        }

                    } catch (IOException ioe) {
                        fail(ioe.getMessage());
                    }
                }
            };
            writer.start();

            // read them back in and check their contents
            FramedInputStream fin = new FramedInputStream();
            DataInputStream din = new DataInputStream(fin);
            for (int ff = 0; ff < FRAMES; ff++) {
                // if (ff % 1000 == 0) System.out.println("Reading frame " + ff);
                try {
                    while (!fin.readFrame(in)) {} // loop!
                    int ints = fin.available() / 4;
                    for (int ii = 0; ii < ints; ii++) assertEquals(ii, din.readInt());
                } catch (IllegalArgumentException iae) {
                    System.err.println("Invariant violation: " + iae.getMessage());
                    System.err.println("While reading frame: " + ff);
                    System.err.println("Frame sizes: " + frameSizes);
                    throw iae;
                }
            }

            writer.join();
            in.close();
            out.close();
        }
    }

    protected static final String STRING1 = "This is a test.";
    protected static final String STRING2 = "This is only a test.";
    protected static final String STRING3 =
        "If this were not a test, there would be meaningful data in " +
        "this frame and someone would probably be enjoying themselves.";

    protected static final String STRING4 =
        "Now is the time for all good men to come to the aid of their country.";
    protected static final String STRING5 = "Every good boy deserves fudge.";
    protected static final String STRING6 = "The quick brown fox jumped over the lazy dog.";

    protected static final String STRING7 = "Third time is the charm.";
}
