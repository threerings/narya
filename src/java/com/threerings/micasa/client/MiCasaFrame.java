//
// $Id: MiCasaFrame.java,v 1.4 2001/12/20 01:10:51 shaper Exp $

package com.threerings.micasa.client;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;

/**
 * Contains the user interface for the MiCasa client application.
 */
public class MiCasaFrame
    extends JFrame implements ControllerProvider
{
    /**
     * Constructs the top-level MiCasa client frame.
     */
    public MiCasaFrame ()
    {
        super("MiCasa Client");
    }

    /**
     * Sets the panel that makes up the entire client display.
     */
    public void setPanel (JPanel panel)
    {
        // remove the old panel
        getContentPane().removeAll();
	// add the new one
	getContentPane().add(panel, BorderLayout.CENTER);
        // swing doesn't properly repaint after adding/removing children
        validate();
    }

    /**
     * Sets the controller for the outermost scope. This controller will
     * handle all actions that aren't handled by controllers of tigher
     * scope.
     */
    public void setController (Controller controller)
    {
        _controller = controller;
    }

    // documentation inherited
    public Controller getController ()
    {
        return _controller;
    }

    protected Controller _controller;
}
