//
// $Id: PlacePanel.java,v 1.1 2002/07/09 21:05:33 mdb Exp $

package com.threerings.crowd.client;

import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;

import com.threerings.crowd.data.PlaceObject;

/**
 * A useful base class for client interfaces which wish to make use of
 * {@link a} JPanel as their top-level {@link PlaceView}.
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
