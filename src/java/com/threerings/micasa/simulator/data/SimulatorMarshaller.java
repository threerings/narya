//
// $Id: SimulatorMarshaller.java,v 1.3 2004/02/25 14:43:37 mdb Exp $

package com.threerings.micasa.simulator.data;

import com.threerings.micasa.simulator.client.SimulatorService;
import com.threerings.parlor.game.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the implementation of the {@link SimulatorService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 *
 * <p> Generated from <code>
 * $Id: SimulatorMarshaller.java,v 1.3 2004/02/25 14:43:37 mdb Exp $
 * </code>
 */
public class SimulatorMarshaller extends InvocationMarshaller
    implements SimulatorService
{
    /** The method id used to dispatch {@link #createGame} requests. */
    public static final int CREATE_GAME = 1;

    // documentation inherited from interface
    public void createGame (Client arg1, GameConfig arg2, String arg3, int arg4)
    {
        sendRequest(arg1, CREATE_GAME, new Object[] {
            arg2, arg3, new Integer(arg4)
        });
    }

    // Class file generated on 12:33:03 08/20/02.
}
