//
// $Id: DownstreamMessage.java,v 1.1 2001/05/22 06:07:59 mdb Exp $

package com.samskivert.cocktail.cher.net;

import com.samskivert.cocktail.cher.io.TypedObject;
import com.samskivert.cocktail.cher.io.TypedObjectFactory;

/**
 * The <code>DownstreamMessage</code> class encapsulates a message in the
 * Distributed Object Protocol that flows from the server to the
 * client. Downstream messages include object subscription, event
 * forwarding and session management.
 */
public class DownstreamMessage
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

    // register our downstream message classes
    static {
        TypedObjectFactory.registerClass(AuthenticationRequest.TYPE,
                                         AuthenticationRequest.class);
        TypedObjectFactory.registerClass(SubscribeRequest.TYPE,
                                         SubscribeRequest.class);
        TypedObjectFactory.registerClass(FetchRequest.TYPE,
                                         FetchRequest.class);
        TypedObjectFactory.registerClass(UnsubscribeNotification.TYPE,
                                         UnsubscribeNotification.class);
        TypedObjectFactory.registerClass(ForwardEventNotification.TYPE,
                                         ForwardEventNotification.class);
        TypedObjectFactory.registerClass(PingNotification.TYPE,
                                         PingNotification.class);
        TypedObjectFactory.registerClass(LogoffNotification.TYPE,
                                         LogoffNotification.class);
    }

    /** The code for an event notification. */
    public static final byte EVENT = 0x00;
    /** The code for a fetch/subscribe object response. */
    public static final byte OBJECT = 0x01;
    /** The code for a request failure. */
    public static final byte FAILURE = 0x02;
    /** The code for a pong response. */
    public static final byte PONG = 0x03;
    /** The code for an end of transmission notification. */
    public static final byte EOT = 0x04;
}
