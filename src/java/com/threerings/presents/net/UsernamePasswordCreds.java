//
// $Id: UsernamePasswordCreds.java,v 1.7 2002/07/23 05:52:49 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class UsernamePasswordCreds extends Credentials
{
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

    public String getPassword ()
    {
        return _password;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        super.writeObject(out);
        out.writeUTF(_password);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        super.readObject(in);
        _password = in.readUTF();
    }

    public String toString ()
    {
        return "[username=" + _username + ", password=" + _password + "]";
    }

    protected String _password;
}
