//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.client;

import com.threerings.presents.data.ClientObject;

/**
 * Provides a means by which to obtain access to a time base object which can be used to convert
 * delta times into absolute times.
 */
public interface TimeBaseService extends InvocationService<ClientObject>
{
    /**
     * Used to communicated the result of a {@link TimeBaseService#getTimeOid} request.
     */
    public static interface GotTimeBaseListener extends InvocationListener
    {
        /**
         * Communicates the result of a successful {@link TimeBaseService#getTimeOid} request.
         */
        void gotTimeOid (int timeOid);
    }

    /**
     * Requests the oid of the specified time base object be fetched.
     */
    void getTimeOid (String timeBase, GotTimeBaseListener listener);
}
