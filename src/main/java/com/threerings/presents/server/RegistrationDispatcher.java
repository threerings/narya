package com.threerings.presents.server;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationReceiver;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.RegistrationMarshaller;

/**
 * Dispatches requests to the {@link RegistrationProvider}.
 */
@Generated(value={"com.threerings.presents.tools.cpp.GenCPPServiceTask"},
           comments="Derived from RegistrationService.java.")
public class RegistrationDispatcher extends InvocationDispatcher<RegistrationMarshaller>
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public RegistrationDispatcher (RegistrationProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public RegistrationMarshaller createMarshaller ()
    {
        return new RegistrationMarshaller();
    }

    @Override
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case RegistrationMarshaller.REGISTER_RECEIVER:
            ((RegistrationProvider)provider).registerReceiver(
                source, (InvocationReceiver.Registration)args[0]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
