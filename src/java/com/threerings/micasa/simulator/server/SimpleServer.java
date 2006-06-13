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

import com.threerings.micasa.server.MiCasaServer;

/**
 * A simple simulator server implementation that extends the MiCasa server
 * and provides no special functionality.
 */
public class SimpleServer extends MiCasaServer
    implements SimulatorServer
{
    // documentation inherited
    public void init (ResultListener obs)
        throws Exception
    {
        super.init();

        // create the simulator manager
        SimulatorManager simmgr = new SimulatorManager();
        simmgr.init(invmgr, plreg, clmgr, omgr, this);

        if (obs != null) {
            // let the initialization observer know that we've started up
            obs.requestCompleted(this);
        }
    }
}
