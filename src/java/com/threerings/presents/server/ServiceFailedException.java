//
// $Id: ServiceFailedException.java,v 1.2 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.server;

/**
 * An exception class for use in concert with invocation services when
 * they need to communicate a failure of some kind and can't use the
 * return value.
 *
 * <p> For example, consider an invitation service:
 *
 * <pre>
 * public class fooManager
 * {
 *     // returns invitation id, throws ServiceFailedException if
 *     // invitation couldn't be processed
 *     public int invite (...)
 *         throws ServiceFailedException
 *     {
 *     }
 * }
 *
 * public class fooProvider
 * {
 *     public void handleInviteRequest (...)
 *     {
 *         try {
 *             int inviteId = _mgr.invite(...);
 *             sendResponse(..., INVITE_RECEIVED, new Integer(inviteId));
 *
 *         } catch (ServiceFailedException sfe) {
 *             sendResponse(..., INVITE_FAILED, sfe.getMessage());
 *         }
 *     }
 * }
 * </pre>
 */
public class ServiceFailedException extends Exception
{
    public ServiceFailedException (String message)
    {
        super(message);
    }
}
