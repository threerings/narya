//
// $Id: NoSuchTileException.java,v 1.2 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

/**
 * Thrown when an attempt is made to retrieve a non-existent tile from the
 * tile manager.
 */
public class NoSuchTileException extends RuntimeException
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
