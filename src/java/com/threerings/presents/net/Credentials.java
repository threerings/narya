//
// $Id: Credentials.java,v 1.9 2002/09/18 21:58:30 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Credentials are supplied by the client implementation and sent along to
 * the server during the authentication process. To provide support for a
 * variety of authentication methods, the credentials class is meant to be
 * subclassed for the particular method (ie. password, auth digest, etc.)
 * in use in a given system.
 *
 * <p> All credentials must provide a username as the username is used to
 * associate network connections with sessions.
 *
 * <p> All derived classes should provide a no argument constructor so
 * that they can be instantiated prior to reconstruction from a data input
 * stream.
 */
public abstract class Credentials implements Streamable
{
    /**
     * Constructs a credentials instance with the specified username.
     */
    public Credentials (String username)
    {
        _username = username;
    }

    /**
     * Constructs a blank credentials instance in preparation for
     * unserializing from the network.
     */
    public Credentials ()
    {
    }

    public String getUsername ()
    {
        return _username;
    }

    /**
     * Writes our custom streamable fields.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        out.writeUTF(_username);
    }

    /**
     * Reads our custom streamable fields.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _username = in.readUTF();
    }

    // documentation inherited
    public int hashCode ()
    {
        return _username.hashCode();
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof Credentials) {
            return _username.equals(((Credentials)other)._username);
        } else {
            return false;
        }
    }

    public String toString ()
    {
        return "[username=" + _username + "]";
    }

    protected String _username;
}
