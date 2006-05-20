//
// $Id: SceneUpdate.java 3760 2005-11-15 03:08:23Z ray $
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

import flash.errors.IllegalOperationError;

import com.threerings.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Used to encapsulate updates to scenes in such a manner that updates can
 * be stored persistently and sent to clients to update their own local
 * copies of scenes.
 */
public class SceneUpdate
    implements Streamable /*, Cloneable */
{
    /**
     * Initializes this scene update such that it will operate on a scene
     * with the specified target scene and version number.
     *
     * @param targetId the id of the scene on which we are to operate.
     * @param targetVersion the version of the scene on which we are to
     * operate.
     */
    public function init (targetId :int, targetVersion :int) :void
    {
        _targetId = targetId;
        _targetVersion = targetVersion;
    }

    /**
     * Returns the scene id for which this update is appropriate.
     */
    public function getSceneId () :int
    {
        return _targetId;
    }

    /**
     * Returns the scene version for which this update is appropriate.
     */
    public function getSceneVersion () :int
    {
        return _targetVersion;
    }

    /**
     * Called to ensure that the scene is in the appropriate state prior
     * to applying the update.
     *
     * @exception IllegalStateException thrown if the update cannot be
     * applied to the scene because it is not in a valid state
     * (appropriate previous updates were not applied, it's the wrong kind
     * of scene, etc.).
     */
    public function validate (model :SceneModel) :void
        //throws IllegalStateException
    {
        if (model.sceneId != _targetId) {
            var errmsg :String = "Wrong target scene, expected id " +
                _targetId + " got id " + model.sceneId;
            throw new IllegalOperationError(errmsg);
        }
        if (model.version != _targetVersion) {
            var errmsg :String = "Target scene not proper version, expected " +
                _targetVersion + " got " + model.version;
            throw new IllegalOperationError(errmsg);
        }
    }

    /**
     * Applies this update to the specified scene model. Derived classes
     * will want to override this method and apply updates of their own,
     * being sure to call <code>super.apply</code>.
     */
    public function apply (model :SceneModel) :void
    {
        // increment the version; disallowing integer overflow
        model.version = Math.max(_targetVersion + 1, model.version);

        // sanity check for the amazing two billion updates
        if (model.version == _targetVersion) {
            Log.getLog(this).warning("Egads! This scene has been updated two" +
                " billion times [model=" + model + ", update=" + this + "].");
        }
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
        //throws IOException
    {
        out.writeInt(_targetId);
        out.writeInt(_targetVersion);
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
        //throws IOException, ClassNotFoundException
    {
        _targetId = ins.readInt();
        _targetVersion = ins.readInt();
    }

    /**
     * Generates a string representation of this instance.
     */
    public function toString () :String
    {
        var buf :StringBuilder = new StringBuilder("[");
        toStringBuf(buf);
        return buf.append("]").toString();
    }

    /**
     * An extensible mechanism for generating a string representation of
     * this instance.
     */
    protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("sceneId=").append(_targetId);
        buf.append(", version=").append(_targetVersion);
    }

    /** The version number of the scene on which we operate. */
    protected var _targetId :int;

    /** The version number of the scene on which we operate. */
    protected var _targetVersion :int;
}
}
