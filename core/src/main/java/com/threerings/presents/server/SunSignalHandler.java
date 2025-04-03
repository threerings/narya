//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.server;

import com.samskivert.util.RunAnywhere;
import com.samskivert.util.SignalUtil;

/**
 * Handles signals using Sun's undocumented Signal class.
 */
public class SunSignalHandler extends AbstractSignalHandler
{
    @Override
    protected boolean registerHandlers ()
    {
        SignalUtil.register(SignalUtil.Number.TERM, new SignalUtil.Handler() {
            public void signalReceived (SignalUtil.Number sig) {
                termReceived();
            }
        });
        SignalUtil.register(SignalUtil.Number.INT, new SignalUtil.Handler() {
            public void signalReceived (SignalUtil.Number sig) {
                intReceived();
            }
        });
        SignalUtil.register(SignalUtil.Number.USR2, new SignalUtil.Handler() {
            public void signalReceived (SignalUtil.Number sig) {
                usr2Received();
            }
        });
        if (!RunAnywhere.isWindows()) {
            SignalUtil.register(SignalUtil.Number.HUP, new SignalUtil.Handler() {
                public void signalReceived (SignalUtil.Number sig) {
                    hupReceived();
                }
            });
        }
        return true;
    }
}
