//
// $Id: SimpleFrame.java,v 1.1 2002/01/16 02:59:08 mdb Exp $

package com.threerings.micasa.simulator.client;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;

/**
 * Contains the user interface for the Simulator client application.
 */
public class SimpleFrame
    extends JFrame implements SimulatorFrame
{
    /**
     * Constructs the top-level Simulator client frame.
     */
    public SimpleFrame ()
    {
        super("Simulator");
    }

    // documentation inherited
    public JFrame getFrame ()
    {
        return this;
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
