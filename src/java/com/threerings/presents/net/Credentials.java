//
// $Id: Credentials.java,v 1.13 2004/03/06 11:29:19 mdb Exp $

package com.threerings.presents.net;

import com.threerings.io.Streamable;
import com.threerings.util.Name;

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
    public Credentials (Name username)
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

    public Name getUsername ()
    {
        return _username;
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

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * An easily extensible method via which derived classes can add to
     * {@link #toString()}'s output.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("username=").append(_username);
    }

    protected Name _username;
}
