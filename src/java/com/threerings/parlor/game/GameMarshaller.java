//
// $Id: GameMarshaller.java,v 1.2 2002/08/20 19:38:14 mdb Exp $

package com.threerings.parlor.game;

import com.threerings.parlor.game.GameService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link GameService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 *
 * <p> Generated from <code>
 * $Id: GameMarshaller.java,v 1.2 2002/08/20 19:38:14 mdb Exp $
 * </code>
 */
public class GameMarshaller extends InvocationMarshaller
    implements GameService
{
    /** The method id used to dispatch {@link #playerReady} requests. */
    public static final int PLAYER_READY = 1;

    // documentation inherited from interface
    public void playerReady (Client arg1)
    {
        sendRequest(arg1, PLAYER_READY, new Object[] {
            
        });
    }

    // Class file generated on 12:33:04 08/20/02.
}
