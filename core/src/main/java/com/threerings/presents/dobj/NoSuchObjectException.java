//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.dobj;

/**
 * A no such object exception is delivered when a subscriber requests
 * access to an object that does not exist.
 */
public class NoSuchObjectException extends ObjectAccessException
{
    public NoSuchObjectException (int oid)
    {
        super("m.no_such_object\t" + oid);
    }
}
