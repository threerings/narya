//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.bureau.server;

import com.threerings.presents.server.PresentsSession;

public class BureauSession extends PresentsSession
{
    @Override // from PresentsSession
    protected void sessionConnectionClosed ()
    {
        super.sessionConnectionClosed();

        // end our session when the connection is closed
        endSession();
    }
}
