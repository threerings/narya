//
// $Id: TestReceiver.java,v 1.3 2001/08/04 02:54:01 mdb Exp $

package com.threerings.cocktail.cher.client.test;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.client.InvocationReceiver;

public class TestReceiver implements InvocationReceiver
{
    public void handleTestNotification (int invid, int one, String two)
    {
        Log.info("Received tell notification [invid=" + invid +
                 ", one=" + one + ", two=" + two + "].");
    }
}
