//
// $Id: TestReceiver.java,v 1.4 2001/10/11 04:07:52 mdb Exp $

package com.threerings.presents.client.test;

import com.threerings.presents.Log;
import com.threerings.presents.client.InvocationReceiver;

public class TestReceiver implements InvocationReceiver
{
    public void handleTestNotification (int invid, int one, String two)
    {
        Log.info("Received tell notification [invid=" + invid +
                 ", one=" + one + ", two=" + two + "].");
    }
}
