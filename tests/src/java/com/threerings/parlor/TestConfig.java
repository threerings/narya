//
// $Id: TestConfig.java,v 1.3 2001/11/08 02:07:36 mdb Exp $

package com.threerings.parlor;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.parlor.game.GameConfig;

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
