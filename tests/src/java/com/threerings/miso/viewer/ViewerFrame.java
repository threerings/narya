//
// $Id: ViewerFrame.java,v 1.33 2002/04/27 02:34:29 mdb Exp $

package com.threerings.miso.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import com.samskivert.swing.util.MenuUtil;

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

        // create the "Settings" menu
        JMenu menuSettings = new JMenu("Settings");
        MenuUtil.addMenuItem(
            menuSettings, "Preferences", this, "handlePreferences");

        // create the menu bar
        JMenuBar bar = new JMenuBar();
        bar.add(menuSettings);

        // add the menu bar to the frame
        setJMenuBar(bar);
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
