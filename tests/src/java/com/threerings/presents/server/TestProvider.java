//
// $Id: TestProvider.java,v 1.2 2001/07/19 07:48:58 mdb Exp $

package com.threerings.cocktail.cher.server.test;

import com.threerings.cocktail.cher.Log;

/**
 * A test of the invocation services.
 */
public class TestProvider
{
    public Object[] handleTestRequest (String one, int two)
    {
        Log.info("Test request [one=" + one + ", two=" + two + "].");
        return null;
    }
}
