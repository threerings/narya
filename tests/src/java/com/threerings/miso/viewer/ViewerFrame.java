//
// $Id: ViewerFrame.java,v 1.32 2002/04/06 04:53:05 mdb Exp $

package com.threerings.miso.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;

/**
 * The viewer frame is the main application window.
 */
public class ViewerFrame extends JFrame
{
    /**
     * Creates a frame in which the viewer application can operate.
     */
    public ViewerFrame (GraphicsConfiguration gc)
    {
	super(gc);

        // set up the frame options
        setTitle("Scene Viewer");
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // set the frame and content panel background to black
        setBackground(Color.black);
        getContentPane().setBackground(Color.black);
    }

    /**
     * Sets the panel displayed by this frame.
     */
    public void setPanel (Component panel)
    {
        // if we had an old panel, remove it
        if (_panel != null) {
            getContentPane().remove(_panel);
        }    

        // now add the new one
        _panel = panel;
	getContentPane().add(_panel, BorderLayout.CENTER);
    }

    protected Component _panel;
}
