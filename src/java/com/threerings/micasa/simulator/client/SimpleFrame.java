//
// $Id: SimpleFrame.java,v 1.4 2004/02/25 14:43:37 mdb Exp $

package com.threerings.micasa.simulator.client;

import javax.swing.JFrame;

import com.samskivert.swing.Controller;

import com.threerings.micasa.client.MiCasaFrame;

/**
 * Contains the user interface for the Simulator client application.
 */
public class SimpleFrame extends MiCasaFrame
    implements SimulatorFrame
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

    protected Controller _controller;
}
