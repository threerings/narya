//
// $Id: SceneImpl.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.whirled.data {

import com.threerings.crowd.data.PlaceConfig;

/**
 * An implementation of the {@link Scene} interface.
 */
public class SceneImpl implements Scene
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and place config.
     */
    public function SceneImpl (
            model :SceneModel = null, config :PlaceConfig = null)
    {
        if (model != null) {
            _model = model;
            _config = config;

        } else {
            _model = SceneModel.blankSceneModel();
        }
    }

    // documentation inherited
    public function getId () :int
    {
        return _model.sceneId;
    }

    // documentation inherited
    public function getName () :String
    {
        return _model.name;
    }

    // documentation inherited
    public function getVersion () :int
    {
        return _model.version;
    }

    // documentation inherited
    public function getPlaceConfig () :PlaceConfig
    {
        return _config;
    }

    // documentation inherited from interface
    public function setId (sceneId :int) :void
    {
        _model.sceneId = sceneId;
    }

    // documentation inherited from interface
    public function setName (name :String) :void
    {
        _model.name = name;
    }

    // documentation inherited from interface
    public function setVersion (version :int) :void
    {
        _model.version = version;
    }

    // documentation inherited from interface
    public function updateReceived (update :SceneUpdate) :void
    {
        try {
            // validate and apply the update
            update.validate(_model);
            update.apply(_model);
        } catch (e :Error) {
            var log :Log = Log.getLog(this);
            log.warning("Error applying update [scene=" + this +
                        ", update=" + update + "].");
            log.logStackTrace(e);
        }
    }

    // documentation inherited from interface
    public function getSceneModel () :SceneModel
    {
        return _model;
    }

    /**
     * Generates a string representation of this instance.
     */
    public function toString () :String
    {
        return "[model=" + _model + ", config=" + _config + "]";
    }

    /** A reference to our scene model. */
    protected var _model :SceneModel;

    /** A reference to our place configuration. */
    protected var _config :PlaceConfig;
}
}
