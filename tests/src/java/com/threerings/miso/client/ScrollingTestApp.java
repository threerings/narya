//
// $Id: ScrollingTestApp.java,v 1.25 2004/08/27 02:21:01 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.miso.client;

import java.awt.DisplayMode;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;

import java.io.IOException;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.Config;

import com.threerings.resource.ResourceManager;

import com.threerings.media.FrameManager;
import com.threerings.media.image.ImageManager;
import com.threerings.media.util.LinePath;
import com.threerings.media.util.Path;

import com.threerings.media.sprite.PathAdapter;
import com.threerings.media.sprite.Sprite;

import com.threerings.media.tile.bundle.BundledTileSetRepository;

import com.threerings.cast.CharacterComponent;
import com.threerings.cast.CharacterDescriptor;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.CharacterSprite;
import com.threerings.cast.NoSuchComponentException;
import com.threerings.cast.bundle.BundledComponentRepository;

import com.threerings.miso.Log;
import com.threerings.miso.MisoConfig;
import com.threerings.miso.tile.MisoTileManager;
import com.threerings.miso.util.MisoContext;

/**
 * Tests the scrolling capabilities of the IsoSceneView.
 */
public class ScrollingTestApp
{
    /**
     * Construct and initialize the scrolling test app.
     */
    public ScrollingTestApp (String[] args)
        throws IOException
    {
        // get the graphics environment
        GraphicsEnvironment env =
            GraphicsEnvironment.getLocalGraphicsEnvironment();

        // get the target graphics device
        GraphicsDevice gd = env.getDefaultScreenDevice();
        Log.info("Graphics device [dev=" + gd +
                 ", mem=" + gd.getAvailableAcceleratedMemory() +
                 ", displayChange=" + gd.isDisplayChangeSupported() +
                 ", fullScreen=" + gd.isFullScreenSupported() + "].");

        // get the graphics configuration and display mode information
        GraphicsConfiguration gc = gd.getDefaultConfiguration();
        DisplayMode dm = gd.getDisplayMode();
        Log.info("Display mode [bits=" + dm.getBitDepth() +
                 ", wid=" + dm.getWidth() + ", hei=" + dm.getHeight() +
                 ", refresh=" + dm.getRefreshRate() + "].");

        // create the window
	_frame = new ScrollingFrame(gc);

        // set up our frame manager
        _framemgr = FrameManager.newInstance(_frame);

        // we don't need to configure anything
        ResourceManager rmgr = new ResourceManager("rsrc");
        rmgr.initBundles(
            null, "config/resource/manager.properties", null, null);
        ImageManager imgr = new ImageManager(rmgr, _frame);
	_tilemgr = new MisoTileManager(rmgr, imgr);
        _tilemgr.setTileSetRepository(
            new BundledTileSetRepository(rmgr, imgr, "tilesets"));

        // hack in some different MisoProperties
        MisoConfig.config = new Config("rsrc/config/miso/scrolling");

	// create the context object
	MisoContext ctx = new ContextImpl();

        // create the various managers
        BundledComponentRepository crepo =
            new BundledComponentRepository(rmgr, imgr, "components");
        CharacterManager charmgr = new CharacterManager(imgr, crepo);

        // create our scene view panel
        _panel = new MisoScenePanel(ctx, MisoConfig.getSceneMetrics());
        _frame.setPanel(_panel);

        // create our "ship" sprite
        String scclass = "navsail", scname = "smsloop";
        try {
            CharacterComponent ccomp = crepo.getComponent(scclass, scname);
            CharacterDescriptor desc = new CharacterDescriptor(
                new int[] { ccomp.componentId }, null);

            // now create the actual sprite and stick 'em in the scene
            _ship = charmgr.getCharacter(desc);
            if (_ship != null) {
                _ship.setFollowingPathAction("sailing");
                _ship.setRestingAction("sailing");
                _ship.setActionSequence("sailing");
                _ship.setLocation(0, 0);
                _panel.addSprite(_ship);
            }

        } catch (NoSuchComponentException nsce) {
            Log.warning("Can't locate ship component [class=" + scclass +
                        ", name=" + scname + "].");
        }

        _ship.addSpriteObserver(new PathAdapter() {
            public void pathCompleted (Sprite sprite, Path path, long when) {
                // keep scrolling for a spell
                if (++_sidx < DX.length) {
                    int x = _ship.getX(), y = _ship.getY();
                    LinePath lpath = new LinePath(
                        x, y, x + DX[_sidx], y + DY[_sidx], 30000l);
                    _ship.move(lpath);
                }
            }
            protected int _sidx = -1;
            protected final int[] DX = { 1620, 0, 1000, -1000, 1000, 2000 };
            protected final int[] DY = { 1400, 1000, 0, 1000, -1000, 1000 };
        });

        // make the panel follow the ship around
        _panel.setFollowsPathable(_ship, MisoScenePanel.CENTER_ON_PATHABLE);
        int x = _ship.getX(), y = _ship.getY();
        _ship.move(new LinePath(x, y, x, y + 1000, 3000l));

        // size and position the window, entering full-screen exclusive
        // mode if available
        if (gd.isFullScreenSupported()) {
            Log.info("Entering full-screen exclusive mode.");
            gd.setFullScreenWindow(_frame);
            _frame.setUndecorated(true);

        } else {
            Log.warning("Full-screen exclusive mode not available.");
            // _frame.pack();
            _frame.setSize(200, 300);
            SwingUtil.centerWindow(_frame);
        }

        try {
            _panel.setSceneModel(new ScrollingScene(ctx));
        } catch (Exception e) {
            Log.warning("Error creating scene: " + e);
            Log.logStackTrace(e);
        }
    }

    /**
     * The implementation of the MisoContext interface that provides
     * handles to the manager objects that offer commonly used services.
     */
    protected class ContextImpl implements MisoContext
    {
	public MisoTileManager getTileManager () {
	    return _tilemgr;
	}

	public FrameManager getFrameManager () {
	    return _framemgr;
	}
    }

    /**
     * Run the application.
     */
    public void run ()
    {
        // show the window
        _frame.show();
        _framemgr.start();
    }

    /**
     * Instantiate the application object and start it running.
     */
    public static void main (String[] args)
    {
        try {
            ScrollingTestApp app = new ScrollingTestApp(args);
            app.run();
        } catch (IOException ioe) {
            System.err.println("Error initializing scrolling app.");
            ioe.printStackTrace();
        }
    }

    /** The tile manager object. */
    protected MisoTileManager _tilemgr;

    /** The frame manager. */
    protected FrameManager _framemgr;

    /** The main application window. */
    protected ScrollingFrame _frame;

    /** The main panel. */
    protected MisoScenePanel _panel;

    /** The ship in the center of our screen. */
    protected CharacterSprite _ship;
}
