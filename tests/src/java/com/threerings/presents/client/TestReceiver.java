//
// $Id: TestReceiver.java,v 1.6 2002/08/14 19:07:59 mdb Exp $

package com.threerings.presents.client;

/**
 * A test of the invocation notification services.
 */
public interface TestReceiver extends InvocationReceiver
{
    /**
     * Dispatches a test notification.
     */
    public void receivedTest (int one, String two);
}
