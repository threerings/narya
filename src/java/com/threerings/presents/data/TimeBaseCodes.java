//
// $Id: TimeBaseCodes.java,v 1.1 2002/05/28 23:14:06 mdb Exp $

package com.threerings.presents.data;

/**
 * Codes and constants relating to the Presents time base services.
 */
public interface TimeBaseCodes
{
    /** The module name for the time services. */
    public static final String MODULE_NAME = "time";

    /** The message identifier for a request to obtain a particular time
     * object. */
    public static final String GET_TIME_OID_REQUEST = "GetTimeOid";

    /** A response generated for a successful getTimeOid request. */
    public static final String TIME_OID_RESPONSE = "TimeOid";

    /** An error response generated for GetTimeOid requests. */
    public static final String NO_SUCH_TIME_BASE = "m.no_such_time_base";
}
