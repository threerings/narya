//
// $Id: WhirledServer.java,v 1.17 2004/02/25 14:50:28 mdb Exp $

package com.threerings.whirled.server;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.whirled.Log;
import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.util.SceneFactory;

/**
 * The whirled server extends the {@link CrowdServer} and provides access
 * to managers and the like that are needed by the Whirled serviecs.
 */
public abstract class WhirledServer extends CrowdServer
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
        screg = new SceneRegistry(invmgr, _screp, createSceneFactory(),
                                  createConfigFactory());

        Log.info("Whirled server initialized.");
    }

    /**
     * Creates the scene repository that will be used by this server.
     *
     * @exception Exception thrown if any error occurs while instantiating
     * or initializing the scene repository.
     */
    protected abstract SceneRepository createSceneRepository ()
        throws Exception;

    /**
     * Creates the scene factory that will be used by our scene registry.
     *
     * @exception Exception thrown if any error occurs while instantiating
     * or initializing the scene repository.
     */
    protected abstract SceneFactory createSceneFactory ()
        throws Exception;

    /**
     * Creates the place config factory that will be used our scene
     * registry.
     *
     * @exception Exception thrown if any error occurs while instantiating
     * or initializing the scene repository.
     */
    protected abstract SceneRegistry.ConfigFactory createConfigFactory ()
        throws Exception;

    /** The scene repository in use by this server. */
    protected SceneRepository _screp;
}
