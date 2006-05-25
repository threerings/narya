//
// $Id: SpotSceneModel.java 3726 2005-10-11 19:17:43Z ray $
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

package com.threerings.whirled.spot.data {

import com.threerings.util.ClassUtil;

import com.threerings.io.Streamable;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.whirled.data.AuxModel;
import com.threerings.whirled.data.SceneModel;

/**
 * The spot scene model extends the standard scene model with information
 * on portals. Portals are referenced by an identifier, unique within the
 * scene and unchanging, so that portals can stably reference the target
 * portal in the scene to which they connect.
 */
public class SpotSceneModel
    implements Streamable, AuxModel
{
    /** An array containing all portals in this scene. */
    public var portals :TypedArray =
        new TypedArray(TypedArray.getJavaType(Portal));

    /** The portal id of the default entrance to this scene. If a body
     * enters the scene without coming from another scene, this is the
     * portal at which they would appear. */
    public var defaultEntranceId :int = -1;

    /**
     * Adds a portal to this scene model.
     */
    public function addPortal (portal :Portal) :void
    {
        portals.push(portal);
    }

    /**
     * Removes a portal from this model.
     */
    public function removePortal (portal :Portal) :void
    {
        for (var ii :int = 0; ii < portals.length; ii++) {
            if (portal.equals(portals[ii])) {
                portals.splice(ii, 1);
                return;
            }
        }
    }

    // documentation inherited from superinterface Cloneable
    public function clone () :Object
    {
        var clazz :Class = ClassUtil.getClass(this);
        var model :SpotSceneModel = new clazz();

        for each (var portal :Portal in portals) {
            model.portals.push(portal.clone());
        }
        return model;
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeField(portals);
        out.writeInt(defaultEntranceId);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        portals = (ins.readField(TypedArray.getJavaType(Portal)) as TypedArray);
        defaultEntranceId = ins.readInt();
    }

    /**
     * Locates and returns the {@link SpotSceneModel} among the auxiliary
     * scene models associated with the supplied scene
     * model. <code>null</code> is returned if no spot scene model could
     * be found.
     */
    public static function getSceneModel (model :SceneModel) :SpotSceneModel
    {
        for each (var aux :AuxModel in model.auxModels) {
            if (aux is SpotSceneModel) {
                return (aux as SpotSceneModel);
            }
        }
        return null;
    }
}
}
