//
// $Id: AccessControl.java,v 1.2 2002/11/01 01:01:27 mdb Exp $

package com.threerings.crowd.server;
import com.threerings.crowd.data.BodyObject;

/**
 * Used to ratify access control on a feature by feature basis.
 */
public interface AccessControl
{
    /** An error code that can be delivered when a user lacks access for a
     * particular feature. */
    public static final String LACK_ACCESS = "m.lack_access";

    /**
     * Checks to see if the specified user has access to the feature with
     * the specified name. Features are named according to their
     * containing package, e.g. <code>crowd.chat.broadcast</code>.
     *
     * @param user the user requesting the feature.
     * @param feature the string identifying the feature.
     */
    public boolean checkAccess (BodyObject user, String feature);
}
