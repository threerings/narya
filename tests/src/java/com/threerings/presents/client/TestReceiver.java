//
// $Id: TestReceiver.java,v 1.2 2001/08/04 02:31:20 mdb Exp $

package com.threerings.cocktail.cher.client.test;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.client.InvocationReceiver;

public class TestReceiver implements InvocationReceiver
{
    public void handleTestNotification (int one, String two)
    {
        Log.info("Received tell notification [one=" + one +
                 ", two=" + two + "].");
    }
}
