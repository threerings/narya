//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.micasa.simulator.server;

import com.samskivert.util.ResultListener;

/**
 * The simulator manager needs a mechanism for faking body object
 * registrations, which is provided by implementations of this interface.
 */
public interface SimulatorServer
{
    /**
     * Called to initialize this server instance.
     *
     * @param obs the observer to notify when the server has finished
     * starting up, or <code>null</code> if no notification is desired.
     *
     * @exception Exception thrown if anything goes wrong initializing the
     * server.
     */
    public void init (ResultListener obs) throws Exception;

    /**
     * Called to perform the main body of server processing. This is
     * called from the server thread and should do the simulator server's
     * primary business.
     */
    public void run ();
}
