//
// $Id: ViewerFrame.java,v 1.2 2001/07/28 01:31:51 shaper Exp $

package com.threerings.miso.viewer;

import com.samskivert.swing.*;
import com.threerings.miso.Log;
import com.threerings.miso.scene.Scene;
import com.threerings.miso.sprite.AnimationManager;
import com.threerings.miso.sprite.SpriteManager;
import com.threerings.miso.viewer.util.ViewerContext;

import java.awt.*;
import java.io.*;
import javax.swing.*;

/**
 * The ViewerFrame is the main application window that constructs and
 * contains the application menu bar and panels and responds to menu
 * events.
 */
class ViewerFrame extends JFrame
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

	// set up the scene view panel with a default scene
        SceneViewPanel svpanel = new SceneViewPanel(_ctx);

        // create the animation manager for the panel
        SpriteManager spritemgr = new SpriteManager();
        
        AnimationManager animmgr = new AnimationManager(spritemgr, svpanel);

	// add the scene view panel
	getContentPane().add(svpanel, BorderLayout.CENTER);
    }

    /** The panel displaying the scene. */
    SceneViewPanel _svpanel;

    /** The context object. */
    protected ViewerContext _ctx;
}
