//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

/**
 * If injected into a presents server, listens for a USR2 Unix signal captured by
 * {@link AbstractSignalHandler}.
 */
public interface SignalReceiver
{
    void received();
}
