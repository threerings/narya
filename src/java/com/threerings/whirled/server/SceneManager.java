//
// $Id: SceneManager.java,v 1.2 2001/08/21 01:07:19 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.cocktail.party.server.PlaceManager;
import com.threerings.whirled.data.Scene;

public class SceneManager extends PlaceManager
{
    public Scene getScene ()
    {
        return _scene;
    }

    /**
     * Called by the scene registry once the scene manager has been
     * created (and initialized), but before it is started up.
     */
    protected void postInit (Scene scene, SceneRegistry screg)
    {
        _scene = scene;
        _screg = screg;
    }

    /**
     * We're fully ready to go, so now we register ourselves with the
     * scene registry which will make us available to the clients and
     * system at large.
     */
    protected void didStartup ()
    {
        super.didStartup();
        _screg.sceneManagerDidInit(this);
    }

    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append("scene=").append(_scene);
    }

    protected Scene _scene;
    protected SceneRegistry _screg;
}
