//
// $Id: TimeBaseService.java,v 1.3 2002/08/14 19:07:54 mdb Exp $

package com.threerings.presents.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Provides a means by which to obtain access to a time base object which
 * can be used to convert delta times into absolute times.
 */
public interface TimeBaseService extends InvocationService
{
    /**
     * Used to communicated the result of a {@link #getTimeOid} request.
     */
    public static interface GotTimeBaseListener extends InvocationListener
    {
        /**
         * Communicates the result of a successful {@link #getTimeOid}
         * request.
         */
        public void gotTimeOid (int timeOid);
    }

    /**
     * Requests the oid of the specified time base object be fetched.
     */
    public void getTimeOid (
        Client client, String timeBase, GotTimeBaseListener listener);
}
