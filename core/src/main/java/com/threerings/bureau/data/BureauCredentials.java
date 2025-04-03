//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.data;

import com.threerings.presents.net.ServiceCreds;

/**
 * Extends the basic credentials to provide bureau-specific fields.
 */
public class BureauCredentials extends ServiceCreds
{
    /**
     * Creates new credentials for a specific bureau.
     */
    public BureauCredentials (String bureauId, String sharedSecret)
    {
        super(bureauId, sharedSecret);
    }

    /**
     * Creates an empty credentials for streaming. Should not be used directly.
     */
    public BureauCredentials ()
    {
    }
}
