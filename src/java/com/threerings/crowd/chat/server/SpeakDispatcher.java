//
// $Id: SpeakDispatcher.java,v 1.2 2002/08/20 19:38:13 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.crowd.chat.SpeakMarshaller;
import com.threerings.crowd.chat.SpeakService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link SpeakProvider}.
 *
 * <p> Generated from <code>
 * $Id: SpeakDispatcher.java,v 1.2 2002/08/20 19:38:13 mdb Exp $
 * </code>
 */
public class SpeakDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public SpeakDispatcher (SpeakProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new SpeakMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case SpeakMarshaller.SPEAK:
            ((SpeakProvider)provider).speak(
                source,
                (String)args[0], ((Byte)args[1]).byteValue()
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
