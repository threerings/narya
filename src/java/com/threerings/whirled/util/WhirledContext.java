//
// $Id: WhirledContext.java,v 1.3 2001/10/11 04:07:54 mdb Exp $

package com.threerings.whirled.util;

import com.threerings.crowd.util.CrowdContext;
import com.threerings.whirled.client.SceneDirector;

/**
 * The whirled context provides access to the various managers, etc. that
 * are needed by the whirled client code.
 */
public interface WhirledContext extends CrowdContext
{
    /**
     * Returns a reference to the scene director.
     */
    public SceneDirector getSceneDirector ();
}
