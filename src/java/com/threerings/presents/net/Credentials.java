//
// $Id: Credentials.java,v 1.3 2001/05/29 03:27:59 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.cocktail.cher.io.TypedObject;
import com.samskivert.cocktail.cher.io.TypedObjectFactory;

/**
 * Credentials are supplied by the client implementation and sent along to
 * the server during the authentication process. To provide support for a
 * variety of authentication methods, the credentials class is meant to be
 * subclassed for the particular method (ie. username + password) in use
 * in a given system.
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
     * Derived classes should override this function to write their fields
     * out to the supplied data output stream. They <em>must</em> be sure
     * to first call <code>super.writeTo()</code>.
     */
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        // we don't do anything here, but we may want to some day
    }

    /**
     * Derived classes should override this function to read their fields
     * from the supplied data input stream. They <em>must</em> be sure to
     * first call <code>super.readFrom()</code>.
     */
    public void readFrom (DataInputStream in)
        throws IOException
    {
        // we don't do anything here, but we may want to some day
    }
}
