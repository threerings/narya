//
// $Id: SimulatorFrame.java,v 1.4 2004/08/27 02:12:52 mdb Exp $
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

package com.threerings.micasa.simulator.client;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;

/**
 * Contains the user interface for the Simulator client application.
 */
public interface SimulatorFrame extends ControllerProvider
{
    /**
     * Returns a reference to the top-level frame that the simulator will
     * use to display everything.
     */
    public JFrame getFrame ();

    /**
     * Sets the panel that makes up the entire client display.
     */
    public void setPanel (JPanel panel);

    /**
     * Sets the controller for the outermost scope. This controller will
     * handle all actions that aren't handled by controllers of higher
     * scope.
     */
    public void setController (Controller controller);
}
