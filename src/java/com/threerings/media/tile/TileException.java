//
// $Id: TileException.java,v 1.1 2001/10/11 00:41:26 shaper Exp $

package com.threerings.media.tile;

/**
 * Thrown when an error occurs while working with tiles.
 */
public class TileException extends Exception
{
    public TileException (String message)
    {
	super(message);
    }
}
