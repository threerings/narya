//
// $Id: SimulatorDispatcher.java,v 1.3 2004/02/25 14:43:37 mdb Exp $

package com.threerings.micasa.simulator.server;

import com.threerings.micasa.simulator.data.SimulatorMarshaller;
import com.threerings.parlor.game.GameConfig;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link SimulatorProvider}.
 *
 * <p> Generated from <code>
 * $Id: SimulatorDispatcher.java,v 1.3 2004/02/25 14:43:37 mdb Exp $
 * </code>
 */
public class SimulatorDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public SimulatorDispatcher (SimulatorProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new SimulatorMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case SimulatorMarshaller.CREATE_GAME:
            ((SimulatorProvider)provider).createGame(
                source,
                (GameConfig)args[0], (String)args[1], ((Integer)args[2]).intValue()
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
