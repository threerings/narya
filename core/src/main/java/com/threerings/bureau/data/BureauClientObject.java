//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.data;

import com.threerings.presents.data.ClientObject;

/**
 * An object representing a Bureau connection. This is currently just a marker class.
 */
public class BureauClientObject extends ClientObject
{
    @Override
    public String toString ()
    {
        return "BUREAU_CLIENT_OBJECT(" + super.toString() + ")";
    }
}
