//
// $Id: DownstreamMessage.java,v 1.7 2001/07/19 19:30:14 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.cocktail.cher.io.TypedObject;

/**
 * The <code>DownstreamMessage</code> class encapsulates a message in the
 * Distributed Object Protocol that flows from the server to the
 * client. Downstream messages include object subscription, event
 * forwarding and session management.
 */
public abstract class DownstreamMessage implements TypedObject
{
    /**
     * All downstream message derived classes should base their typed
     * object code on this base value.
     */
    public static final short TYPE_BASE = 200;

    /**
     * The message id of the upstream message with which this downstream
     * message is associated (or -1 if it is not associated with any
     * upstream message). Because not every downstream message class cares
     * to provide an upstream message id, this field is not serialized
     * when the base downstream message class is serialized. Thus derived
     * classes that care about message id should take care to initialize,
     * serialize and unserialize the value theirselves.
     */
    public short messageId = -1;

    /**
     * Each downstream message derived class must provide a zero argument
     * constructor so that the <code>TypedObjectFactory</code> can create
     * a new instance of said class prior to unserializing it.
     */
    public DownstreamMessage ()
    {
        // nothing to do...
    }

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

    public String toString ()
    {
        return "[msgid=" + messageId + "]";
    }
}
