//
// $Id: DummyClientSceneRepository.java,v 1.7 2004/08/27 02:21:05 mdb Exp $
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

package com.threerings.whirled;

import java.io.IOException;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.NoSuchSceneException;

/**
 * The dummy scene repository just pretends to load and store scenes, but
 * in fact it just creates new blank scenes when requested to load a scene
 * and does nothing when requested to save one.
 */
public class DummyClientSceneRepository implements SceneRepository
{
    // documentation inherited
    public SceneModel loadSceneModel (int sceneId)
        throws IOException, NoSuchSceneException
    {
        Log.info("Creating dummy scene model [id=" + sceneId + "].");
        return new SceneModel();
    }

    // documentation inherited
    public void storeSceneModel (SceneModel model)
        throws IOException
    {
        // nothing doing
    }

    // documentation inherited
    public void deleteSceneModel (int sceneId)
        throws IOException
    {
        // nothing doing
    }
}
