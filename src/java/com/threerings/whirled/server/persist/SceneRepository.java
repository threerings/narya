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

package com.threerings.whirled.server.persist;

import com.samskivert.io.PersistenceException;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.NoSuchSceneException;
import com.threerings.whirled.util.UpdateList;

/**
 * The scene repository provides the basic interface for loading and
 * updating scene data. It is used by the scene registry and though more
 * scene related persistence services may be needed in a full-fledged
 * application, the scene repository only encapsulates those needed by the
 * scene registry and other services provided by the Whirled framework.
 */
public interface SceneRepository
{
    /**
     * Fetches the model for the scene with the specified scene id.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to load the scene data.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public SceneModel loadSceneModel (int sceneId)
        throws PersistenceException, NoSuchSceneException;

    /**
     * Fetches the set of updates associated with the specified scene.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to load the scene updates.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public UpdateList loadUpdates (int sceneId)
        throws PersistenceException, NoSuchSceneException;

    /**
     * Applise the supplied scene update to persistent representation of
     * its associated scene, then stores the update persistently for
     * future invocations of the server to load. <em>Note:</em> the scene
     * update will have already been applied to the supplied scene model.
     *
     * @exception PersistenceException thrown if an error occurs
     * attempting to apply the scene update.
     */
    public void applyAndRecordUpdate (SceneModel model, SceneUpdate update)
        throws PersistenceException;
}
