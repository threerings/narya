//
// $Id: StreamableUtilTest.java,v 1.2 2002/04/15 16:34:36 shaper Exp $

package com.threerings.presents.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.util.StringUtil;

/**
 * Tests the {@link StreamableUtil} class.
 */
public class StreamableUtilTest extends TestCase
{
    public StreamableUtilTest ()
    {
        super(StreamableUtilTest.class.getName());
    }

    public static class Boink extends SimpleStreamableObject
    {
        public String foo;
        public int bar;
        public float baz;

        public Boink ()
        {
        }

        public Boink (String foo, int bar, float baz)
        {
            this.foo = foo;
            this.bar = bar;
            this.baz = baz;
        }

        public boolean equals (Object other)
        {
            Boink bo = (Boink)other;
            return (bo.foo.equals(foo) &&
                    bo.bar == bar &&
                    bo.baz == baz);
        }
    }

    public static class Blink extends Boink
    {
        public int bizz;

        public Blink ()
        {
        }

        public Blink (String foo, int bar, float baz, int bizz )
        {
            super(foo, bar, baz);
            this.bizz = bizz;
        }

        public boolean equals (Object other)
        {
            Blink bo = (Blink)other;
            return (bo.foo.equals(foo) &&
                    bo.bar == bar &&
                    bo.baz == baz &&
                    bo.bizz == bizz);
        }
    }

    public void runTest ()
    {
        int bcount = 15;
        Boink[] boinks = new Boink[bcount];
        for (int i = 0; i < bcount; i++) {
            if (i % 2 == 0) {
                boinks[i] = new Boink(Integer.toString(i), i,
                                      (float)i + (float)0.5);
            } else {
                boinks[i] = new Blink(Integer.toString(i), i,
                                      (float)i + (float)0.5, 100-i);
            }
        }
        // System.out.println("boinks: " + StringUtil.toString(boinks));

        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            DataOutputStream dout = new DataOutputStream(bout);
            StreamableUtil.writeStreamables(dout, boinks);
            dout.flush();

            byte[] data = bout.toByteArray();
            // System.out.println(bcount + " boinks takes up " +
            // data.length + " bytes.");

            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            DataInputStream din = new DataInputStream(bin);
            Boink[] nboinks = (Boink[])StreamableUtil.readStreamables(din);

            // make sure all went well
            assertTrue("boinks == nboinks", Arrays.equals(boinks, nboinks));

            // System.out.println("nboinks: " + StringUtil.toString(nboinks));

        } catch (Exception e) {
            e.printStackTrace(System.err);
            fail("exception: " + e);
        }
    }

    public static Test suite ()
    {
        return new StreamableUtilTest();
    }

    public static void main (String[] args)
    {
        StreamableUtilTest test = new StreamableUtilTest();
        test.runTest();
    }
}
