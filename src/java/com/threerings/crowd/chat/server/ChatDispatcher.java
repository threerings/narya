//
// $Id: ChatDispatcher.java,v 1.6 2003/09/18 17:53:48 mdb Exp $

package com.threerings.crowd.chat.server;

import com.threerings.crowd.chat.client.ChatService;
import com.threerings.crowd.chat.client.ChatService.TellListener;
import com.threerings.crowd.chat.data.ChatMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.InvocationListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link ChatProvider}.
 */
public class ChatDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public ChatDispatcher (ChatProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new ChatMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case ChatMarshaller.TELL:
            ((ChatProvider)provider).tell(
                source,
                (String)args[0], (String)args[1], (TellListener)args[2]
            );
            return;

        case ChatMarshaller.BROADCAST:
            ((ChatProvider)provider).broadcast(
                source,
                (String)args[0], (InvocationListener)args[1]
            );
            return;

        case ChatMarshaller.AWAY:
            ((ChatProvider)provider).away(
                source,
                (String)args[0]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
