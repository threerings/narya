//
// $Id: UsernamePasswordCreds.java,v 1.4 2001/05/30 23:58:31 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UsernamePasswordCreds extends Credentials
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
        super.writeTo(out);
        out.writeUTF(_username);
        out.writeUTF(_password);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _username = in.readUTF();
        _password = in.readUTF();
    }

    protected String _username;
    protected String _password;
}
