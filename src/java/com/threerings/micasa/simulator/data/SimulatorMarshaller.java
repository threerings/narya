//
// $Id: SimulatorMarshaller.java,v 1.1 2002/08/14 19:07:51 mdb Exp $

package com.threerings.micasa.simulator.data;

import com.threerings.micasa.simulator.client.SimulatorService;
import com.threerings.parlor.game.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link SimulatorService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
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

    // Class file generated on 00:26:00 08/11/02.
}
