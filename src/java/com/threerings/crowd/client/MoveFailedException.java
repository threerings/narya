//
// $Id: MoveFailedException.java,v 1.1 2002/06/14 01:40:16 ray Exp $

package com.threerings.crowd.client;

/**
 * An exception that indicates that the server did not allow us to move.
 */
public class MoveFailedException extends Exception
{
    public MoveFailedException (String message)
    {
        super(message);
    }
}
