//
// $Id: TestConfig.java,v 1.1 2001/10/03 03:45:44 mdb Exp $

package com.threerings.parlor.test;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.parlor.data.GameConfig;

public class TestConfig extends GameConfig
{
    /** The foozle parameter. */
    public int foozle;

    public Class getControllerClass ()
    {
        return TestController.class;
    }

    public String getManagerClassName ()
    {
        return "com.threerings.parlor.test.TestManager";
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeInt(foozle);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        foozle = in.readInt();
    }

    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", foozle=").append(foozle);
    }
}
