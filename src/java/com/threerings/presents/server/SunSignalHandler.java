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

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.samskivert.util.RunAnywhere;

import static com.threerings.presents.Log.log;

/**
 * Handles signals using Sun's undocumented Signal class.
 */
public class SunSignalHandler extends AbstractSignalHandler
    implements SignalHandler
{
    // from interface SignalHandler
    public void handle (Signal sig)
    {
        SignalHandler chain = null;
        if (sig.getName().equals("INT")) {
            intReceived();
        } else if (sig.getName().equals("HUP")) {
            hupReceived();
        } else {
            log.warning("Received unknown signal '" + sig.getName() + "'.");
        }
    }

    protected boolean registerHandlers ()
    {
        // we don't track and call the chained handlers for INT and HUP because those exit the JVM
        // which we do not want to do
        Signal.handle(new Signal("INT"), this);
        if (!RunAnywhere.isWindows()) {
            Signal.handle(new Signal("HUP"), this);
        }
        return true;
    }
}
