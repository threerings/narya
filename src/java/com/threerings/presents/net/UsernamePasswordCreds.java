//
// $Id: UsernamePasswordCreds.java,v 1.1 2001/05/22 06:08:00 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UsernamePasswordCreds
{
    public static final short TYPE = TYPE_BASE + 0;

    /**
     * Zero argument constructor used when unserializing an instance.
     */
    public UsernamePasswordCreds ()
    {
        super();
    }

    /**
     * Construct credentials with the supplied username and password.
     */
    public UsernamePasswordCreds (String username, String password)
    {
        _username = username;
        _password = password;
    }

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeUTF(_username);
        out.writeUTF(_password);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        _username = in.readUTF();
        _password = in.readUTF();
    }

    protected String _username;
    protected String _password;
}
