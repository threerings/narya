//
// $Id: Registry.java,v 1.2 2001/05/30 23:58:31 mdb Exp $

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

        // register our downstream message classes
        TypedObjectFactory.registerClass(AuthResponse.TYPE,
                                         AuthResponse.class);
        TypedObjectFactory.registerClass(EventNotification.TYPE,
                                         EventNotification.class);
        TypedObjectFactory.registerClass(ObjectResponse.TYPE,
                                         ObjectResponse.class);
        TypedObjectFactory.registerClass(FailureResponse.TYPE,
                                         FailureResponse.class);
        TypedObjectFactory.registerClass(PongNotification.TYPE,
                                         PongNotification.class);

        // register our credential classes
        TypedObjectFactory.registerClass(UsernamePasswordCreds.TYPE,
                                         UsernamePasswordCreds.class);

    }
}
