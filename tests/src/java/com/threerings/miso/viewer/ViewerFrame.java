//
// $Id: ViewerFrame.java,v 1.22 2001/10/17 22:16:04 shaper Exp $

package com.threerings.miso.viewer;

import java.awt.*;
import javax.swing.*;

import com.samskivert.swing.*;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.scene.AmbulatorySprite;
import com.threerings.miso.scene.CharacterManager;
import com.threerings.miso.tile.TileUtil;
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
	_ctx = ctx;

        // get a reference on our various manager objects
        SpriteManager spritemgr = new SpriteManager();
        TileManager tilemgr = _ctx.getTileManager();

        // construct the character manager from which we obtain our sprite
        CharacterManager charmgr = new CharacterManager(
            ctx.getConfig(), tilemgr);

        // add the test character sprite to the sprite manager
	AmbulatorySprite sprite = charmgr.getCharacter(TSID_CHAR);
        if (sprite != null) {
            sprite.setLocation(300, 300);
            spritemgr.addSprite(sprite);
            Log.info("Created sprite [sprite=" + sprite + "].");
        }

	// set up the scene view panel with a default scene
        ViewerSceneViewPanel svpanel =
	    new ViewerSceneViewPanel(_ctx, spritemgr, sprite);

	// add the main panel to the frame
	getContentPane().add(svpanel);
    }

    /** The tileset id for the character tiles. */
    protected static final int TSID_CHAR = 1011;

    /** The panel displaying the scene. */
    ViewerSceneViewPanel _svpanel;

    /** The context object. */
    protected ViewerContext _ctx;
}
