//
// $Id: ViewerFrame.java,v 1.17 2001/09/05 00:45:27 shaper Exp $

package com.threerings.miso.viewer;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

import com.samskivert.swing.*;

import com.threerings.media.sprite.*;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.scene.AmbulatorySprite;
import com.threerings.miso.tile.TileUtil;
import com.threerings.miso.viewer.util.ViewerContext;

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

        // add the test character sprite to the sprite manager
        MultiFrameImage[] anims =
            TileUtil.getAmbulatorySpriteFrames(tilemgr, TSID_CHAR);
        AmbulatorySprite sprite = new AmbulatorySprite(300, 300, anims);
	sprite.setVelocity(6, 6);
        spritemgr.addSprite(sprite);

	// create a top-level panel to manage everything
	JPanel top = new JPanel();
	GroupLayout gl = new HGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	top.setLayout(gl);

	top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	// set up the scene view panel with a default scene
        ViewerSceneViewPanel svpanel =
	    new ViewerSceneViewPanel(_ctx, spritemgr, sprite);
	top.add(svpanel, GroupLayout.FIXED);

	// create a sub-panel to hold the side panels
	JPanel sub = new JPanel();
	gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	gl.setJustification(GroupLayout.TOP);
	sub.setLayout(gl);

	// add the various side-panels
	sub.add(new StatusPanel(), GroupLayout.FIXED);
	sub.add(new ManagementPanel());
	sub.add(new ChatPanel());

	// add the sub-panel to the main panel
	top.add(sub);

	// add the main panel to the frame
	getContentPane().add(top, BorderLayout.CENTER);
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
    ViewerSceneViewPanel _svpanel;

    /** The context object. */
    protected ViewerContext _ctx;
}
