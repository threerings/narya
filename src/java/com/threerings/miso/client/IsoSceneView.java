//
// $Id: IsoSceneView.java,v 1.1 2001/07/12 22:38:03 shaper Exp $

package com.threerings.cocktail.miso.scene;

import com.threerings.cocktail.miso.tile.Tile;
import com.threerings.cocktail.miso.tile.TileManager;

import java.awt.*;

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

	_offGraphics.setColor(Color.white);
	_offGraphics.fillRect(0, 0, _bounds.width, _bounds.height);

	renderScene(_offGraphics, _viewX, _viewY);

	g.drawImage(_offImage, 0, 0, null);
    }

    protected void renderScene (Graphics g, int x, int y)
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

		g.drawImage(tile.img, screenX, screenY, null);

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
	_offImage = target.createImage(_bounds.width, _bounds.height);
	_offGraphics = _offImage.getGraphics();
    }

    protected static final int OFF_X = -Tile.WIDTH;
    protected static final int OFF_Y = 0;

    protected static final int DEF_BOUNDS_WIDTH = 600;
    protected static final int DEF_BOUNDS_HEIGHT = 600;

    protected Rectangle _bounds;

    protected int       _viewX, _viewY;

    protected Graphics  _offGraphics;
    protected Image     _offImage;

    protected Scene _scene;

    protected TileManager _tmgr;
}
