//
// $Id: ChatDispatcher.java,v 1.1 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.crowd.chat.ChatMarshaller;
import com.threerings.crowd.chat.ChatService;
import com.threerings.crowd.chat.ChatService.TellListener;
import com.threerings.presents.client.Client;
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

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
