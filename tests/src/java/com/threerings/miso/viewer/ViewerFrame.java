//
// $Id: ViewerFrame.java,v 1.25 2001/10/25 16:33:20 shaper Exp $

package com.threerings.miso.viewer;

import javax.swing.JFrame;

import com.threerings.media.sprite.SpriteManager;

import com.threerings.miso.Log;
import com.threerings.miso.viewer.util.ViewerContext;

/**
 * The viewer frame is the main application window.
 */
public class ViewerFrame extends JFrame
{
    public ViewerFrame ()
    {
	super("Scene Viewer");

        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Initialize the frame with the context object.
     */
    public void init (ViewerContext ctx)
    {
        // create the sprite manager
        SpriteManager spritemgr = new SpriteManager();

        // add the main panel to the frame
	getContentPane().add(new ViewerSceneViewPanel(ctx, spritemgr));
    }
}
