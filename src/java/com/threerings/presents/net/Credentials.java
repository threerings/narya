//
// $Id: Credentials.java,v 1.7 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.presents.io.TypedObject;
import com.threerings.presents.io.TypedObjectFactory;

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
public abstract class Credentials implements TypedObject
{
    /**
     * All credential derived classes should base their typed object code
     * on this base value.
     */
    public static final short TYPE_BASE = 300;

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
     * Derived classes should override this function to write their fields
     * out to the supplied data output stream. They <em>must</em> be sure
     * to first call <code>super.writeTo()</code>.
     */
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        out.writeUTF(_username);
    }

    /**
     * Derived classes should override this function to read their fields
     * from the supplied data input stream. They <em>must</em> be sure to
     * first call <code>super.readFrom()</code>.
     */
    public void readFrom (DataInputStream in)
        throws IOException
    {
        _username = in.readUTF();
    }

    public String toString ()
    {
        return "[username=" + _username + "]";
    }

    protected String _username;
}
