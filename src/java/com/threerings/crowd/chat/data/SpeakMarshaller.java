//
// $Id: SpeakMarshaller.java,v 1.1 2002/08/14 19:07:49 mdb Exp $

package com.threerings.crowd.chat;

import com.threerings.crowd.chat.SpeakService;
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

    // Class file generated on 19:01:34 08/12/02.
}
