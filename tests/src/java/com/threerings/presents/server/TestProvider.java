//
// $Id: TestProvider.java,v 1.3 2001/07/19 18:08:20 mdb Exp $

package com.threerings.cocktail.cher.server.test;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.server.InvocationProvider;

/**
 * A test of the invocation services.
 */
public class TestProvider extends InvocationProvider
{
    public Object[] handleTestRequest (String one, int two)
    {
        Log.info("Test request [one=" + one + ", two=" + two + "].");
        return createResponse("TestSucceeded", one, new Integer(two));
    }
}
