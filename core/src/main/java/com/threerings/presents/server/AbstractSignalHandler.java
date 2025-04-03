//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.google.inject.Inject;
import static com.threerings.presents.Log.log;

/**
 * A base class that is used to wire up signal handling in one of a couple of possible ways.
 */
public abstract class AbstractSignalHandler
{
    /**
     * Initializes this signal handler.
     */
    public boolean init ()
    {
        return registerHandlers();
    }

    /**
     * Signal handler implementations should wire themselves up in the call to this method.
     *
     * @return true if the handlers were successfully wired up, false if they were not able to be
     * wired up.
     */
    protected abstract boolean registerHandlers ();

    /**
     * Implementations should call this method when a SIGTERM is received.
     */
    protected void termReceived ()
    {
        log.info("Shutdown initiated by TERM signal.");
        _server.queueShutdown();
    }

    /**
     * Implementations should call this method when a SIGINT is received.
     */
    protected void intReceived ()
    {
        log.info("Shutdown initiated by INT signal.");
        _server.queueShutdown();
    }

    /**
     * Implementations should call this method when a SIGHUP is received.
     */
    protected void hupReceived ()
    {
        log.info(_repmgr.generateReport(ReportManager.DEFAULT_TYPE));
    }

    protected void usr2Received ()
    {
        if (_usr2receiver != null) {
            _usr2receiver.received();
        }
    }

    @Inject(optional=true) protected SignalReceiver _usr2receiver;
    @Inject protected PresentsServer _server;
    @Inject protected ReportManager _repmgr;
}
