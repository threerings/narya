//
// $Id: ViewerFrame.java,v 1.27 2001/11/02 03:09:10 shaper Exp $

package com.threerings.miso.viewer;

import javax.swing.JFrame;

import com.threerings.media.ImageManager;
import com.threerings.media.sprite.SpriteManager;

import com.threerings.miso.Log;
import com.threerings.miso.util.MisoUtil;
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
        // create the various managers
        SpriteManager spritemgr = new SpriteManager();

        // add the main panel to the frame
	getContentPane().add(new ViewerSceneViewPanel(ctx, spritemgr));
    }
}
