//
// $Id: TimeBaseProvider.java,v 1.1 2002/05/28 23:14:06 mdb Exp $

package com.threerings.presents.server;

import java.util.HashMap;
import com.samskivert.util.ResultListener;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.TimeBaseCodes;
import com.threerings.presents.data.TimeBaseObject;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessException;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.dobj.Subscriber;

/**
 * Provides the server-side of the time base services. The time base
 * services provide a means by which delta times can be sent over the
 * network which are expanded based on a shared base time into full time
 * stamps.
 */
public class TimeBaseProvider extends InvocationProvider
    implements TimeBaseCodes
{
    /**
     * Registers the time provider with the appropriate managers. Called
     * by the presents server at startup.
     */
    public static void init (InvocationManager invmgr, RootDObjectManager omgr)
    {
        // we'll need these later
        _invmgr = invmgr;
        _omgr = omgr;

        // register an invocation provider instance
        _invmgr.registerProvider(MODULE_NAME, new TimeBaseProvider());
    }

    /**
     * Creates a time base object which can subsequently be fetched by the
     * client and used to send delta times.
     *
     * @param timeBase the name of the time base to create.
     * @param resl the result listener that will be informed when the time
     * base object is created or if the creation fails.
     */
    public static void createTimeBase (
        final String timeBase, final ResultListener resl)
    {
        _omgr.createObject(TimeBaseObject.class, new Subscriber () {
            public void objectAvailable (DObject object) {
                // stuff it into our table
                _timeBases.put(timeBase, object);
                // and notify the listener
                resl.requestCompleted(object);
            }

            public void requestFailed (int oid, ObjectAccessException cause) {
                Log.warning("Ack. Unable to create time base object " +
                            "[timeBase=" + timeBase +
                            ", cause=" + cause + "].");
                // notify the listener that we're borked
                resl.requestFailed(cause);
            }
        });
    }

    /**
     * Returns the named timebase object, or null if no time base object
     * has been created with that name.
     */
    public static TimeBaseObject getTimeBase (String timeBase)
    {
        return (TimeBaseObject)_timeBases.get(timeBase);
    }

    /**
     * Processes a request from a client to fetch the oid of the specified
     * time object.
     */
    public void handleGetTimeOidRequest (
        ClientObject source, int invid, String timeBase)
        throws ServiceFailedException
    {
        // look up the time base object in question
        TimeBaseObject time = getTimeBase(timeBase);
        if (time == null) {
            throw new ServiceFailedException(NO_SUCH_TIME_BASE);
        }
        // and send the response
        sendResponse(source, invid, TIME_OID_RESPONSE,
                     new Integer(time.getOid()));
    }

    /** Used to keep track of our time base objects. */
    protected static HashMap _timeBases = new HashMap();

    /** The invocation manager with which we interoperate. */
    protected static InvocationManager _invmgr;

    /** The distributed object manager with which we interoperate. */
    protected static RootDObjectManager _omgr;
}
