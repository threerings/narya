//
// $Id: ScrollingFrame.java,v 1.2 2002/02/19 07:21:33 mdb Exp $

package com.threerings.miso.scene;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;

/**
 * The main application window.
 */
public class ScrollingFrame extends JFrame
{
    /**
     * Creates a frame in which the scrolling test app can operate.
     */
    public ScrollingFrame (GraphicsConfiguration gc)
    {
	super(gc);

        // set up the frame options
        setTitle("Scene scrolling test");
        // setUndecorated(true);
        setIgnoreRepaint(true);
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
