//
// $Id: ParlorContext.java,v 1.2 2001/10/11 04:07:52 mdb Exp $

package com.threerings.parlor.util;

import com.threerings.crowd.util.CrowdContext;
import com.threerings.parlor.client.ParlorDirector;

/**
 * The parlor context provides access to the various managers, etc. that
 * are needed by the parlor client code.
 */
public interface ParlorContext extends CrowdContext
{
    /**
     * Returns a reference to the parlor director.
     */
    public ParlorDirector getParlorDirector ();
}
