//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.data;

import com.threerings.util.Name;

/**
 * Represents an authenticated bureau client.
 */
public class BureauAuthName extends Name
{
    public BureauAuthName (String bureauId)
    {
        super(bureauId);
    }
}
