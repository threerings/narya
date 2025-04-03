//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A separate invoker thread on which we perform client authentication. This allows the normal
 * server operation to proceed even in the event that our authentication services have gone down
 * and attempts to authenticate cause long timeouts and blockage.
 */
@Singleton
public class PresentsAuthInvoker extends ReportingInvoker
{
    @Inject public PresentsAuthInvoker (PresentsDObjectMgr omgr, ReportManager repmgr)
    {
        super("presents.AuthInvoker", omgr, repmgr);
        setDaemon(true);
    }
}
