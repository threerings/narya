//
// $Id: NoSuchTileSetException.java,v 1.1 2001/10/11 00:41:26 shaper Exp $

package com.threerings.media.tile;

/**
 * Thrown when an attempt is made to retrieve a non-existent tile set
 * from the tile set manager.
 */
public class NoSuchTileSetException extends TileException
{
    public NoSuchTileSetException (int tsid)
    {
	super("No such tile set [tsid=" + tsid + "]");
	_tsid = tsid;
    }

    public int getTileSetId ()
    {
	return _tsid;
    }

    protected int _tsid;
}
