//
// $Id: SpeakMarshaller.java,v 1.3 2003/06/03 21:41:33 ray Exp $

package com.threerings.crowd.chat.data;

import com.threerings.crowd.chat.client.SpeakService;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link SpeakService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class SpeakMarshaller extends InvocationMarshaller
    implements SpeakService
{
    /** The method id used to dispatch {@link #speak} requests. */
    public static final int SPEAK = 1;

    // documentation inherited from interface
    public void speak (Client arg1, String arg2, byte arg3)
    {
        sendRequest(arg1, SPEAK, new Object[] {
            arg2, new Byte(arg3)
        });
    }

}
