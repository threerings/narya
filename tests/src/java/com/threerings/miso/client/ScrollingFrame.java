//
// $Id: ScrollingFrame.java,v 1.4 2003/01/31 23:11:07 mdb Exp $

package com.threerings.miso.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GraphicsConfiguration;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.VGroupLayout;

import com.threerings.media.SafeScrollPane;

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

        // create some interface elements to go with our scrolling panel
        VGroupLayout vgl = new VGroupLayout(VGroupLayout.STRETCH);
        vgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        getContentPane().setLayout(vgl);

        vgl = new VGroupLayout(VGroupLayout.NONE);
        vgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        JPanel stuff = new JPanel(vgl);
        for (int i = 0; i < 10; i++) {
            stuff.add(new JButton("Button " + i));
        }
        getContentPane().add(new SafeScrollPane(stuff));
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
	getContentPane().add(_panel, 0);
    }

    protected Component _panel;
}
