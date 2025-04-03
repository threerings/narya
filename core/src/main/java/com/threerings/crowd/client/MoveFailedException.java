//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

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
