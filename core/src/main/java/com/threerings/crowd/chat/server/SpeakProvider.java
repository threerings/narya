//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.crowd.chat.server;

import javax.annotation.Generated;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.chat.client.SpeakService;

/**
 * Defines the server-side of the {@link SpeakService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SpeakService.java.")
public interface SpeakProvider extends InvocationProvider
{
    /**
     * Handles a {@link SpeakService#speak} request.
     */
    void speak (ClientObject caller, String arg1, byte arg2);
}
