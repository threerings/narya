//
// $Id: ChatMarshaller.java,v 1.2 2002/08/20 19:38:13 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.crowd.chat.ChatService;
import com.threerings.crowd.chat.ChatService.TellListener;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link ChatService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 *
 * <p> Generated from <code>
 * $Id: ChatMarshaller.java,v 1.2 2002/08/20 19:38:13 mdb Exp $
 * </code>
 */
public class ChatMarshaller extends InvocationMarshaller
    implements ChatService
{
    // documentation inherited
    public static class TellMarshaller extends ListenerMarshaller
        implements TellListener
    {
        /** The method id used to dispatch {@link #tellSucceeded}
         * responses. */
        public static final int TELL_SUCCEEDED = 1;

        // documentation inherited from interface
        public void tellSucceeded ()
        {
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, TELL_SUCCEEDED,
                               new Object[] {  }));
        }

        // documentation inherited
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case TELL_SUCCEEDED:
                ((TellListener)listener).tellSucceeded(
                    );
                return;

            default:
                super.dispatchResponse(methodId, args);
            }
        }
    }

    /** The method id used to dispatch {@link #tell} requests. */
    public static final int TELL = 1;

    // documentation inherited from interface
    public void tell (Client arg1, String arg2, String arg3, TellListener arg4)
    {
        TellMarshaller listener4 = new TellMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, TELL, new Object[] {
            arg2, arg3, listener4
        });
    }

    // Class file generated on 12:33:02 08/20/02.
}
