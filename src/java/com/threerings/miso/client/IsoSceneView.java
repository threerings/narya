//
// $Id: IsoSceneView.java,v 1.2 2001/07/14 00:21:23 shaper Exp $

package com.threerings.cocktail.miso.scene;

import com.threerings.cocktail.miso.Log;
import com.threerings.cocktail.miso.media.ImageManager;
import com.threerings.cocktail.miso.tile.Tile;
import com.threerings.cocktail.miso.tile.TileManager;

import java.awt.*;
import java.awt.image.*;

/**
 * The IsoSceneView provides an isometric graphics view of a
 * particular scene.
 */
public class IsoSceneView implements SceneView
{
    public IsoSceneView (TileManager tmgr)
    {
	_tmgr = tmgr;
	
	_viewX = 0;
	_viewY = 0;

	_bounds = new Rectangle(0, 0, DEF_BOUNDS_WIDTH, DEF_BOUNDS_HEIGHT);
    }

    public void paint (Graphics g)
    {
	Graphics2D g2 = (Graphics2D)g;

//	Image img = ImageManager.getImage("/home/shaper/workspace/cocktail/rsrc/media/miso/tiles-base.png");
//	_scene.tiles[10][10][0].img =
//	    ImageManager.getImageCropped(img, 32, 0, 32, 16);

	_offGraphics.setColor(Color.red);
	_offGraphics.fillRect(0, 0, _bounds.width, _bounds.height);

	// draw the full scene into the offscreen image buffer
	renderScene(_offGraphics, _viewX, _viewY);

	// copy offscreen buffer to the given graphics context
	g2.drawImage(_offImg, null, 0, 0);
    }

    protected void renderScene (Graphics2D g2, int x, int y)
    {
	int mapX = x / Tile.HALF_WIDTH;
	int xOff = x & (Tile.HALF_WIDTH - 1);

	int mapY = y / Tile.HEIGHT;
	int yOff = y & Tile.HEIGHT - 1;

	int xa   = xOff - yOff;
	int ya   = (xOff >> 1) + (yOff >> 1);

	int mx = mapX;
	int my = mapY;

	int screenY = OFF_Y + (Tile.HALF_HEIGHT - ya);

	for (int ii = 0; ii < Scene.TILE_HEIGHT; ii++) {
	    int tx = mx;
	    int ty = my;

	    int screenX = OFF_X + (Tile.HALF_WIDTH - xa);

	    int length = Scene.TILE_WIDTH;
	    if ((ii & 1) == 1) {
		length++;
		screenX -= Tile.HALF_WIDTH;
	    }

	    for (int j = 0; j < length; j++) {
		// TODO: draw layers L1+.
		Tile tile = _scene.tiles[tx][ty][Scene.LAYER_BASE];

		g2.drawImage(tile.img, screenX, screenY, null);
		//Log.info("Drawing tile [tx=" + tx + ", ty=" + ty +
		//     ", sx=" + screenX + ", sy=" + screenY +
		//     ", img=" + tile.img + "].");
		
		screenX += Tile.WIDTH;

		if ((tx += 1) > Scene.TILE_WIDTH - 1) tx = 0;
		if ((ty -= 1) < 0) ty = Scene.TILE_HEIGHT - 1;
	    }

	    screenY += Tile.HALF_HEIGHT;

	    if ((ii & 1) == 1) {
		if ((mx += 1) > Scene.TILE_WIDTH - 1) mx = 0;
	    } else {
		if ((my += 1) > Scene.TILE_HEIGHT - 1) my = 0;
	    }
	}
    }

    public void setScene (Scene scene)
    {
	_scene = scene;
    }

    public void setTarget (Component target)
    {
	_target = target;

	int width = _bounds.width, height = _bounds.height;
	_offImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	_offGraphics = _offImg.createGraphics();

	Log.info("Creating offscreen [width=" + width + ", height=" +
		 height + "].");
    }

    protected static final int OFF_X = -Tile.WIDTH;
    protected static final int OFF_Y = 0;

    protected static final int DEF_BOUNDS_WIDTH = 600;
    protected static final int DEF_BOUNDS_HEIGHT = 600;

    protected BufferedImage _offImg;
    protected Graphics2D _offGraphics;

    protected Rectangle _bounds;
    protected int _viewX, _viewY;
    protected Scene _scene;
    protected Component _target;
    protected TileManager _tmgr;
}
