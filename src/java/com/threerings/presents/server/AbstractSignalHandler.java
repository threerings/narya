//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.presents.server;

import static com.threerings.presents.Log.log;

/**
 * A base class that is used to wire up signal handling in one of a couple of possible ways.
 */
public abstract class AbstractSignalHandler
{
    public boolean init (PresentsServer server)
    {
        _server = server;
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
        log.info(PresentsServer.generateReport());
    }

    protected PresentsServer _server;
}
