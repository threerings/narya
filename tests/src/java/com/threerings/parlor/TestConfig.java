//
// $Id: TestConfig.java,v 1.4 2002/07/23 05:55:40 mdb Exp $

package com.threerings.parlor;

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

    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", foozle=").append(foozle);
    }
}
