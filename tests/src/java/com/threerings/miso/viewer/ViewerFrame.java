//
// $Id: ViewerFrame.java,v 1.29 2002/01/08 22:16:59 shaper Exp $

package com.threerings.miso.viewer;

import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;

import javax.swing.JFrame;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.VGroupLayout;

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
        // setUndecorated(true);
        setIgnoreRepaint(true);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // center the scene view within the frame
        GroupLayout gl = new VGroupLayout();
        gl.setJustification(GroupLayout.CENTER);
        gl.setOffAxisJustification(GroupLayout.CENTER);
        getContentPane().setLayout(gl);

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
	getContentPane().add(_panel);
    }

    protected Component _panel;
}
