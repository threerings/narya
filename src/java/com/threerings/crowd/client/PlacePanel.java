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

package com.threerings.crowd.client;

import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;

import com.threerings.crowd.data.PlaceObject;

/**
 * A useful base class for client interfaces which wish to make use of a
 * {@link JPanel} as their top-level {@link PlaceView}.
 */
public class PlacePanel extends JPanel
    implements ControllerProvider, PlaceView
{
    /**
     * Constructs a place panel with the specified controller which will
     * be made availabel via the {@link ControllerProvider} interface.
     */
    public PlacePanel (PlaceController controller)
    {
        _controller = controller;
    }

    // documentation inherited from interface
    public Controller getController ()
    {
        return _controller;
    }

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    /** A reference to the controller with which we interoperate. */
    protected PlaceController _controller;
}
