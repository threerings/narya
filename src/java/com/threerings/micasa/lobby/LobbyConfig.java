//
// $Id: LobbyConfig.java,v 1.1 2001/10/09 00:48:34 mdb Exp $

package com.threerings.micasa.lobby;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.party.data.PlaceConfig;

public class LobbyConfig extends PlaceConfig
{
    // documentation inherited
    public Class getControllerClass ()
    {
        return LobbyController.class;
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.micasa.lobby.LobbyManager";
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
    }
}
