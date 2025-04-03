//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
