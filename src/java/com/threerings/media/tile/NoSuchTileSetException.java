//
// $Id: NoSuchTileSetException.java,v 1.2 2003/01/13 22:49:46 mdb Exp $

package com.threerings.media.tile;

/**
 * Thrown when an attempt is made to retrieve a non-existent tile set from
 * the tile set manager.
 */
public class NoSuchTileSetException extends Exception
{
    public NoSuchTileSetException (String tileSetName)
    {
	super("No tile set named '" + tileSetName + "'");
    }

    public NoSuchTileSetException (int tileSetId)
    {
	super("No tile set with id '" + tileSetId + "'");
    }
}
