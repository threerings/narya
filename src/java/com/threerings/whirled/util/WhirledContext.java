//
// $Id: WhirledContext.java,v 1.2 2001/10/01 22:16:02 mdb Exp $

package com.threerings.whirled.util;

import com.threerings.cocktail.party.util.PartyContext;
import com.threerings.whirled.client.SceneDirector;

/**
 * The whirled context provides access to the various managers, etc. that
 * are needed by the whirled client code.
 */
public interface WhirledContext extends PartyContext
{
    /**
     * Returns a reference to the scene director.
     */
    public SceneDirector getSceneDirector ();
}
