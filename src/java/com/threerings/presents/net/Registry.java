//
// $Id: Registry.java,v 1.5 2001/06/09 23:39:04 mdb Exp $

package com.threerings.cocktail.cher.net;

import com.threerings.cocktail.cher.dobj.DObject;
import com.threerings.cocktail.cher.io.TypedObject;
import com.threerings.cocktail.cher.io.TypedObjectFactory;

/**
 * The registry provides a single place where all typed objects that are
 * exchanged between the client and the server can be registered with the
 * typed object factory.
 */
public class Registry
{
    /**
     * Must be called once by the client and the server to ensure that all
     * typed objects are registered with the typed object system.
     */
    public static void registerTypedObjects ()
    {
        // register our upstream message classes
        TypedObjectFactory.registerClass(AuthRequest.TYPE,
                                         AuthRequest.class);
        TypedObjectFactory.registerClass(SubscribeRequest.TYPE,
                                         SubscribeRequest.class);
        TypedObjectFactory.registerClass(UnsubscribeRequest.TYPE,
                                         UnsubscribeRequest.class);
        TypedObjectFactory.registerClass(ForwardEventRequest.TYPE,
                                         ForwardEventRequest.class);
        TypedObjectFactory.registerClass(PingRequest.TYPE,
                                         PingRequest.class);
        TypedObjectFactory.registerClass(LogoffRequest.TYPE,
                                         LogoffRequest.class);

        // register our downstream message classes
        TypedObjectFactory.registerClass(AuthResponse.TYPE,
                                         AuthResponse.class);
        TypedObjectFactory.registerClass(EventNotification.TYPE,
                                         EventNotification.class);
        TypedObjectFactory.registerClass(ObjectResponse.TYPE,
                                         ObjectResponse.class);
        TypedObjectFactory.registerClass(FailureResponse.TYPE,
                                         FailureResponse.class);
        TypedObjectFactory.registerClass(PongResponse.TYPE,
                                         PongResponse.class);

        // register our credential classes
        TypedObjectFactory.registerClass(UsernamePasswordCreds.TYPE,
                                         UsernamePasswordCreds.class);

    }
}
