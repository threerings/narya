//
// $Id: UsernamePasswordCreds.java,v 1.6 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

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
        super(username);
        _password = password;
    }

    public short getType ()
    {
        return TYPE;
    }

    public String getPassword ()
    {
        return _password;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeUTF(_password);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _password = in.readUTF();
    }

    public String toString ()
    {
        return "[username=" + _username + ", password=" + _password + "]";
    }

    protected String _password;
}
