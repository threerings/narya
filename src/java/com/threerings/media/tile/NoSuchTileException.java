//
// $Id: NoSuchTileException.java,v 1.3 2003/02/04 21:38:46 mdb Exp $

package com.threerings.media.tile;

/**
 * Thrown when an attempt is made to retrieve a non-existent tile from the
 * tile manager.
 */
public class NoSuchTileException extends RuntimeException
{
    public NoSuchTileException (TileSet set, int tid)
    {
	super("No such tile [set=" + set + ", tid=" + tid + "]");
	_tid = tid;
    }

    public int getTileId ()
    {
	return _tid;
    }

    protected int _tid;
}
