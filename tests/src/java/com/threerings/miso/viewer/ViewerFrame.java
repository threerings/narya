//
// $Id: ViewerFrame.java,v 1.1 2001/07/25 17:38:15 shaper Exp $

package com.threerings.miso.viewer;

import com.samskivert.swing.*;
import com.samskivert.swing.util.MenuUtil;
import com.threerings.miso.Log;
import com.threerings.miso.viewer.util.ViewerContext;
import com.threerings.miso.scene.Scene;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * The ViewerFrame is the main application window that constructs and
 * contains the application menu bar and panels and responds to menu
 * events.
 */
class ViewerFrame extends JFrame implements ActionListener
{
    public ViewerFrame ()
    {
	super("Scene Viewer");
    }

    /**
     * Initialize the frame with the context object.
     */
    public void init (ViewerContext ctx)
    {
	_ctx = ctx;

	// set up the menu bar
	createMenuBar();

	// create a top-level panel to manage everything
	JPanel top = new JPanel();
	GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	top.setLayout(gl);

	// set up the scene view panel with a default scene
        SceneViewPanel svpanel = new SceneViewPanel(_ctx);
//      setScene(new Scene(_tilemgr, Scene.SID_INVALID));
  	top.add(svpanel);

	// now add our top-level panel
	getContentPane().add(top, BorderLayout.CENTER);
    }

    /**
     * Create the menu bar and menu items and add them to the frame.
     */
    public void createMenuBar ()
    {
        KeyStroke accel = null;

	// create the "File" menu
	JMenu menuFile = new JMenu("File");
        accel = KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK);
        MenuUtil.addMenuItem(this, menuFile, "Quit", KeyEvent.VK_Q, accel);

	// create the menu bar
	JMenuBar bar = new JMenuBar();
	bar.add(menuFile);

	// add the menu bar to the frame
	setJMenuBar(bar);
    }

    /**
     * Handle menu item selections.
     */
    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();

        if (cmd.equals("Quit")) {
	    System.exit(0);

	} else {
	    Log.warning("Unknown action command [cmd=" + cmd + "].");
	}
    }

    /** The context object. */
    protected ViewerContext _ctx;
}
