package com.threerings.presents.data;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationReceiver;
import com.threerings.presents.client.RegistrationService;

/**
 * Provides the implementation of the {@link RegistrationService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.cpp.GenCPPServiceTask"},
           comments="Derived from RegistrationService.java.")
public class RegistrationMarshaller extends InvocationMarshaller
    implements RegistrationService
{
    /** The method id used to dispatch {@link #registerReceiver} requests. */
    public static final int REGISTER_RECEIVER = 1;

    // from interface RegistrationService
    public void registerReceiver (InvocationReceiver.Registration arg1)
    {
        sendRequest(REGISTER_RECEIVER, new Object[] {
            arg1
        });
    }
}
