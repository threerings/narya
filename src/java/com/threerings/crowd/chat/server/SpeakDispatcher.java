//
// $Id: SpeakDispatcher.java,v 1.4 2004/02/25 14:41:47 mdb Exp $

package com.threerings.crowd.chat.server;

import com.threerings.crowd.chat.data.SpeakMarshaller;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link SpeakProvider}.
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
