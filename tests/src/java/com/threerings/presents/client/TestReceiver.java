//
// $Id: TestReceiver.java,v 1.5 2001/11/08 02:07:36 mdb Exp $

package com.threerings.presents.client;

import com.threerings.presents.Log;

public class TestReceiver implements InvocationReceiver
{
    public void handleTestNotification (int invid, int one, String two)
    {
        Log.info("Received tell notification [invid=" + invid +
                 ", one=" + one + ", two=" + two + "].");
    }
}
