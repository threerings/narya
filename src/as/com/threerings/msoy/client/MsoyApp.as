package com.threerings.msoy.client {

import mx.core.Application;
import mx.events.FlexEvent;

public class MsoyApp extends Application
{
    public function MsoyApp ()
    {
        super();
        addEventListener(attachApplication, nowShowing);
    }

    /**
     * Called once we're ready to go.
     */
    private function nowShowing (event :FlexEvent) :void
    {
        removeEventListener(attachApplication, nowShowing);
    }
}
}
