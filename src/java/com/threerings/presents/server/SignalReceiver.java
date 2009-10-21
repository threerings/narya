package com.threerings.presents.server;

/**
 * If injected into a presents server, listens for a USR2 Unix signal captured by
 * {@link AbstractSignalHandler}.
 */
public interface SignalReceiver
{
    void received();
}
