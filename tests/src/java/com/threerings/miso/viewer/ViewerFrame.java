//
// $Id: ViewerFrame.java,v 1.23 2001/10/23 02:03:49 shaper Exp $

package com.threerings.miso.viewer;

import javax.swing.JFrame;

import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.scene.CharacterManager;
import com.threerings.miso.viewer.util.ViewerContext;

/**
 * The ViewerFrame is the main application window that constructs and
 * contains the application menu bar and panels and responds to menu
 * events.
 */
public class ViewerFrame extends JFrame
{
    public ViewerFrame ()
    {
	super("Scene Viewer");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Initialize the frame with the context object.
     */
    public void init (ViewerContext ctx)
    {
        // get a reference on our various manager objects
        SpriteManager spritemgr = new SpriteManager();
        TileManager tilemgr = ctx.getTileManager();

        // construct the character manager from which we obtain our sprite
        CharacterManager charmgr = new CharacterManager(
            ctx.getConfig(), tilemgr);

	// set up the scene view panel with a default scene
        ViewerSceneViewPanel svpanel =
	    new ViewerSceneViewPanel(ctx, spritemgr, charmgr);

	// add the main panel to the frame
	getContentPane().add(svpanel);
    }
}
