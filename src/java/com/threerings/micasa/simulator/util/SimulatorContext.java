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

package com.threerings.micasa.simulator.util;

import com.threerings.parlor.util.ParlorContext;

import com.threerings.micasa.simulator.client.SimulatorFrame;
import com.threerings.micasa.simulator.data.SimulatorInfo;

/**
 * The simulator context encapsulates the contexts of all of the services
 * that are used by the simulator client so that we can pass around one
 * single context implementation that provides all of the necessary
 * components to all of the services in use.
 */
public interface SimulatorContext
    extends ParlorContext
{
    /** Returns a reference to the primary user interface frame. */
    public SimulatorFrame getFrame ();

    /** Returns a reference to the simulator info describing the game and
     * other details of the simulation. */
    public SimulatorInfo getSimulatorInfo ();
}
