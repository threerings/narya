//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.data;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.bureau.client.BureauService;

/**
 * Provides the implementation of the {@link BureauService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from BureauService.java.")
public class BureauMarshaller extends InvocationMarshaller<ClientObject>
    implements BureauService
{
    /** The method id used to dispatch {@link #agentCreated} requests. */
    public static final int AGENT_CREATED = 1;

    // from interface BureauService
    public void agentCreated (int arg1)
    {
        sendRequest(AGENT_CREATED, new Object[] {
            Integer.valueOf(arg1)
        });
    }

    /** The method id used to dispatch {@link #agentCreationFailed} requests. */
    public static final int AGENT_CREATION_FAILED = 2;

    // from interface BureauService
    public void agentCreationFailed (int arg1)
    {
        sendRequest(AGENT_CREATION_FAILED, new Object[] {
            Integer.valueOf(arg1)
        });
    }

    /** The method id used to dispatch {@link #agentDestroyed} requests. */
    public static final int AGENT_DESTROYED = 3;

    // from interface BureauService
    public void agentDestroyed (int arg1)
    {
        sendRequest(AGENT_DESTROYED, new Object[] {
            Integer.valueOf(arg1)
        });
    }

    /** The method id used to dispatch {@link #bureauError} requests. */
    public static final int BUREAU_ERROR = 4;

    // from interface BureauService
    public void bureauError (String arg1)
    {
        sendRequest(BUREAU_ERROR, new Object[] {
            arg1
        });
    }

    /** The method id used to dispatch {@link #bureauInitialized} requests. */
    public static final int BUREAU_INITIALIZED = 5;

    // from interface BureauService
    public void bureauInitialized (String arg1)
    {
        sendRequest(BUREAU_INITIALIZED, new Object[] {
            arg1
        });
    }
}
