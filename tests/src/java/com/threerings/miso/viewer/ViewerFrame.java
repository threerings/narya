//
// $Id: ViewerFrame.java,v 1.35 2004/08/27 02:21:01 mdb Exp $
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

package com.threerings.miso.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;

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

    /**
     * Dummy callback method.
     */
    public void handlePreferences (ActionEvent event)
    {
        System.err.println("Nothing doing!");
    }

    protected Component _panel;
}
