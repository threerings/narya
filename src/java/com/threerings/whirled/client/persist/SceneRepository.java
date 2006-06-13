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

package com.threerings.whirled.client.persist;

import java.io.IOException;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The scene repository provides access to a persistent repository of
 * scene information.
 *
 * @see SceneModel
 */
public interface SceneRepository
{
    /**
     * Fetches the model for the scene with the specified id.
     *
     * @exception IOException thrown if an error occurs attempting to load
     * the scene data.
     * @exception NoSuchSceneException thrown if no scene exists with the
     * specified scene id.
     */
    public SceneModel loadSceneModel (int sceneId)
        throws IOException, NoSuchSceneException;

    /**
     * Updates or inserts this scene model as appropriate.
     *
     * @exception IOException thrown if an error occurs attempting to
     * access the repository.
     */
    public void storeSceneModel (SceneModel model)
        throws IOException;

    /**
     * Deletes the specified scene model from the repository.
     *
     * @exception IOException thrown if an error occurs attempting to
     * access the repository.
     */
    public void deleteSceneModel (int sceneId)
        throws IOException;
}
