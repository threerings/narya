//
// $Id: SimpleFrame.java,v 1.3 2002/07/12 03:49:37 mdb Exp $

package com.threerings.micasa.simulator.client;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.util.SwingUtil;

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
