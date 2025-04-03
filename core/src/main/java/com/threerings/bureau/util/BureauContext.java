//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.util;

import com.threerings.presents.util.PresentsContext;

import com.threerings.bureau.client.BureauDirector;

/**
 * Defines the objects held on a bureau client. This includes usual set of objects found on a
 * standard presents client.
 */
public interface BureauContext extends PresentsContext
{
    /**
     * Access the director object.
     */
    BureauDirector getBureauDirector ();

    /**
     * Access the bureau id.
     */
    String getBureauId ();
}
