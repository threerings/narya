//
// $Id: WhirledServer.java,v 1.15 2002/10/21 20:56:21 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.whirled.Log;
import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.server.persist.DummySceneRepository;

/**
 * The whirled server extends the {@link CrowdServer} and provides access
 * to managers and the like that are needed by the Whirled serviecs.
 */
public class WhirledServer extends CrowdServer
{
    /** The scene registry. */
    public static SceneRegistry screg;

    /**
     * Initializes all of the server services and prepares for operation.
     */
    public void init ()
        throws Exception
    {
        // do the base server initialization
        super.init();

        // configure the client to use our whirled client
        clmgr.setClientClass(WhirledClient.class);

        // create the scene repository
        _screp = createSceneRepository();

        // create our scene registry
        screg = new SceneRegistry(invmgr, _screp);

        Log.info("Whirled server initialized.");
    }

    /**
     * Creates the scene repository that will be used by this server. If a
     * derived class wishes to use a particular kind of scene repository
     * (which they most likely will), they should override this method and
     * instantiate the scene repository of their choosing.
     *
     * @exception Exception thrown if any error occurs while instantiating
     * or initializing the scene repository.
     */
    protected SceneRepository createSceneRepository ()
        throws Exception
    {
        return new DummySceneRepository();
    }

    /** The scene repository in use by this server. */
    protected SceneRepository _screp;
}
