//
// $Id: ViewerFrame.java,v 1.3 2001/07/30 15:38:52 shaper Exp $

package com.threerings.miso.viewer;

import com.samskivert.swing.*;
import com.threerings.miso.Log;
import com.threerings.miso.scene.Scene;
import com.threerings.miso.sprite.*;
import com.threerings.miso.tile.TileManager;
import com.threerings.miso.viewer.util.ViewerContext;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * The ViewerFrame is the main application window that constructs and
 * contains the application menu bar and panels and responds to menu
 * events.
 */
class ViewerFrame extends JFrame implements WindowListener
{
    public ViewerFrame ()
    {
	super("Scene Viewer");

        addWindowListener(this);
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

	// set up the scene view panel with a default scene
        SceneViewPanel svpanel = new SceneViewPanel(_ctx, spritemgr);

        // add the test character sprite to the sprite manager
        MobileSprite ms = new MobileSprite(300, 300, tilemgr, TSID_CHAR);
        spritemgr.addSprite(ms);

        // create the animation manager for this panel
        AnimationManager animmgr = new AnimationManager(spritemgr, svpanel);

	// add the scene view panel
	getContentPane().add(svpanel, BorderLayout.CENTER);
    }

    /** WindowListener interface methods */

    public void windowClosing (WindowEvent e) {
        System.exit(0);
    }

    public void windowOpened (WindowEvent e) { }
    public void windowClosed (WindowEvent e) { }
    public void windowIconified (WindowEvent e) { }
    public void windowDeiconified (WindowEvent e) { }
    public void windowActivated (WindowEvent e) { }
    public void windowDeactivated (WindowEvent e) { }

    /** The tileset id for the character tiles. */
    protected static final int TSID_CHAR = 1003;

    /** The panel displaying the scene. */
    SceneViewPanel _svpanel;

    /** The context object. */
    protected ViewerContext _ctx;
}
