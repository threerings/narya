//
// $Id: NoSuchTileException.java,v 1.1 2001/10/11 00:41:26 shaper Exp $

package com.threerings.media.tile;

/**
 * Thrown when an attempt is made to retrieve a non-existent tile from the
 * tile manager.
 */
public class NoSuchTileException extends TileException
{
    public NoSuchTileException (int tid)
    {
	super("No such tile [tid=" + tid + "]");
	_tid = tid;
    }

    public int getTileId ()
    {
	return _tid;
    }

    protected int _tid;
}
