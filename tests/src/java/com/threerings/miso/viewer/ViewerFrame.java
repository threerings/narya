//
// $Id: ViewerFrame.java,v 1.28 2001/11/18 04:09:21 mdb Exp $

package com.threerings.miso.viewer;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * The viewer frame is the main application window.
 */
public class ViewerFrame extends JFrame
{
    /**
     * Creates a frame in which the viewer application can operate.
     */
    public ViewerFrame ()
    {
	super("Scene Viewer");
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void setPanel (JPanel panel)
    {
        // if we had an old panel, remove it
        if (_panel != null) {
            getContentPane().remove(_panel);
        }    

        // now add the new one
        _panel = panel;
	getContentPane().add(_panel);
    }

    protected JPanel _panel;
}
