//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;

/**
 * Entites that wish to resolve client objects must implement this
 * interface so as to partake in the asynchronous process of client
 * object resolution.
 */
public interface ClientResolutionListener
{
    /**
     * Called when resolution completed successfully.
     */
    void clientResolved (Name username, ClientObject clobj);

    /**
     * Called when resolution fails.
     */
    void resolutionFailed (Name username, Exception reason);
}
