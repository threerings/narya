//
// $Id: AccessControl.java,v 1.1 2002/10/31 21:32:39 mdb Exp $

package com.threerings.crowd.server;
import com.threerings.crowd.data.BodyObject;

/**
 * Used to ratify access control on a feature by feature basis.
 */
public interface AccessControl
{
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
