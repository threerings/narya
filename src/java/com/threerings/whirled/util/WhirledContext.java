//
// $Id: WhirledContext.java,v 1.1 2001/08/14 06:51:07 mdb Exp $

package com.threerings.whirled.util;

import com.threerings.cocktail.party.util.PartyContext;
import com.threerings.whirled.client.SceneManager;

/**
 * The whirled context provides access to the various managers, etc. that
 * are needed by the whirled client code.
 */
public interface WhirledContext extends PartyContext
{
    /**
     * Returns a reference to the scene manager.
     */
    public SceneManager getSceneManager ();
}
