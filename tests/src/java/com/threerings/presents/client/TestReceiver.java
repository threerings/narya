//
// $Id: TestReceiver.java,v 1.1 2001/07/19 19:18:06 mdb Exp $

package com.threerings.cocktail.cher.client.test;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.client.InvocationReceiver;

public class TestReceiver extends InvocationReceiver
{
    public void handleTestNotification (int one, String two)
    {
        Log.info("Received tell notification [one=" + one +
                 ", two=" + two + "].");
    }
}
