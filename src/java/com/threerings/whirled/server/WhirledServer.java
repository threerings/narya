//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
