//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.data;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.crowd.client.BodyService;

/**
 * Provides the implementation of the {@link BodyService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from BodyService.java.")
public class BodyMarshaller extends InvocationMarshaller<ClientObject>
    implements BodyService
{
    /** The method id used to dispatch {@link #setIdle} requests. */
    public static final int SET_IDLE = 1;

    // from interface BodyService
    public void setIdle (boolean arg1)
    {
        sendRequest(SET_IDLE, new Object[] {
            Boolean.valueOf(arg1)
        });
    }
}
