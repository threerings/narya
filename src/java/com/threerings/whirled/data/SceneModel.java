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

package com.threerings.whirled.data;

import com.samskivert.util.ArrayUtil;

import com.threerings.io.SimpleStreamableObject;

/**
 * The scene model is the bare bones representation of the data for a
 * scene in the Whirled system. From the scene model, one would create an
 * instance of {@link Scene}.
 *
 * <p> The scene model is what is loaded from the scene repositories and
 * what is transmitted over the wire when communicating scenes from the
 * server to the client.
 */
public class SceneModel extends SimpleStreamableObject
    implements Cloneable
{
    /** This scene's unique identifier. */
    public int sceneId;

    /** The human readable name of this scene. */
    public String name;

    /** The version number of this scene. Versions are incremented
     * whenever modifications are made to a scene so that clients can
     * determine whether or not they have the latest version of a
     * scene. */
    public int version;

    /** Auxiliary scene model information. */
    public AuxModel[] auxModels = new AuxModel[0];

    /**
     * Adds the specified auxiliary model to this scene model.
     */
    public void addAuxModel (AuxModel auxModel)
    {
        auxModels = (AuxModel[])ArrayUtil.append(auxModels, auxModel);
    }

    // documentation inherited
    public Object clone ()
        throws CloneNotSupportedException
    {
        SceneModel model = (SceneModel)super.clone();
        model.auxModels = new AuxModel[auxModels.length];
        for (int ii = 0; ii < auxModels.length; ii++) {
            model.auxModels[ii] = (AuxModel)auxModels[ii].clone();
        }
        return model;
    }

    /**
     * Creates and returns a blank scene model.
     */
    public static SceneModel blankSceneModel ()
    {
        SceneModel model = new SceneModel();
        populateBlankSceneModel(model);
        return model;
    }

    /**
     * Populates a blank scene model with blank values.
     */
    protected static void populateBlankSceneModel (SceneModel model)
    {
        model.sceneId = -1;
        model.name = "<blank>";
        model.version = 0;
    }
}
