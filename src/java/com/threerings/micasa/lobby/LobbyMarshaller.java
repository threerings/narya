//
// $Id: LobbyMarshaller.java,v 1.2 2002/08/20 19:38:14 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.micasa.lobby.LobbyService;
import com.threerings.micasa.lobby.LobbyService.CategoriesListener;
import com.threerings.micasa.lobby.LobbyService.LobbiesListener;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import java.util.List;

/**
 * Provides the implementation of the {@link LobbyService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 *
 * <p> Generated from <code>
 * $Id: LobbyMarshaller.java,v 1.2 2002/08/20 19:38:14 mdb Exp $
 * </code>
 */
public class LobbyMarshaller extends InvocationMarshaller
    implements LobbyService
{
    // documentation inherited
    public static class LobbiesMarshaller extends ListenerMarshaller
        implements LobbiesListener
    {
        /** The method id used to dispatch {@link #gotLobbies}
         * responses. */
        public static final int GOT_LOBBIES = 1;

        // documentation inherited from interface
        public void gotLobbies (List arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, GOT_LOBBIES,
                               new Object[] { arg1 }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_LOBBIES:
                ((LobbiesListener)listener).gotLobbies(
                    (List)args[0]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    // documentation inherited
    public static class CategoriesMarshaller extends ListenerMarshaller
        implements CategoriesListener
    {
        /** The method id used to dispatch {@link #gotCategories}
         * responses. */
        public static final int GOT_CATEGORIES = 1;

        // documentation inherited from interface
        public void gotCategories (String[] arg1)
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, GOT_CATEGORIES,
                               new Object[] { arg1 }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_CATEGORIES:
                ((CategoriesListener)listener).gotCategories(
                    (String[])args[0]);
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #getCategories} requests. */
    public static final int GET_CATEGORIES = 1;

    // documentation inherited from interface
    public void getCategories (Client arg1, CategoriesListener arg2)
    {
        CategoriesMarshaller listener2 = new CategoriesMarshaller();
        listener2.listener = arg2;
        sendRequest(arg1, GET_CATEGORIES, new Object[] {
            listener2
        });
    }

    /** The method id used to dispatch {@link #getLobbies} requests. */
    public static final int GET_LOBBIES = 2;

    // documentation inherited from interface
    public void getLobbies (Client arg1, String arg2, LobbiesListener arg3)
    {
        LobbiesMarshaller listener3 = new LobbiesMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, GET_LOBBIES, new Object[] {
            arg2, listener3
        });
    }

    // Class file generated on 12:33:03 08/20/02.
}
