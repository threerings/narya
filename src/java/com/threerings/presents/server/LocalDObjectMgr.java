//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

import java.awt.EventQueue;

import com.threerings.presents.dobj.DEvent;

/**
 * A special version of the distributed object manager, modified to
 * operate on the AWT thread so that it can run in a client with a GUI and
 * provide a "light" server for local operation of a normally distributed
 * application.
 */
public class LocalDObjectMgr extends PresentsDObjectMgr
{
    // documentation inherited
    public synchronized boolean isDispatchThread ()
    {
        return EventQueue.isDispatchThread();
    }

    // documentation inherited
    public void postEvent (final DEvent event)
    {
        EventQueue.invokeLater(new Runnable() {
            public void run () {
                processUnit(event);
            }
        });
    }

    // documentation inherited
    public void postRunnable (Runnable unit)
    {
        // we just pass this right on to the AWT event queue rather than
        // running them through processUnit() which would basically just
        // call run() though we lose a tiny bit of inconsequential
        // accounting data
        EventQueue.invokeLater(unit);
    }
}
