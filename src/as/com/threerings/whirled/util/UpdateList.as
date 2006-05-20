//
// $Id: UpdateList.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.whirled.util {

import com.threerings.whirled.data.SceneUpdate;

/**
 * A list specialized for storing {@link SceneUpdate} objects.
 */
public class UpdateList
{
    public function UpdateList ()
    {
        _updates = new Array();
    }

    /**
     * Adds an update to this list. The update must follow appropriately
     * the chain of updates established by the updates already in the list
     * (meaning it must operate on one version higher than the most recent
     * update already in the list).
     */
    public function addUpdate (update :SceneUpdate) :void
    {
        if (_minVersion == -1) {
            // this is our first update, so we initialize our min version
            _minVersion = update.getSceneVersion();

        } else {
            var gotVersion :int = update.getSceneVersion();
            var expVersion :int = _minVersion + _updates.length;
            if (gotVersion > expVersion) {
                Log.getLog(this).warning("Update continuity broken, " +
                    "flushing list [got=" + update + ", expect=" + expVersion +
                    ", ucount=" + _updates.length + "].");
                // flush out our old updates and start anew from here
                _updates.length = 0;
                _minVersion = expVersion;

            } else if (gotVersion < expVersion) {
                // we somehow got an update that's older than updates we
                // already have? wick wick wack
                throw new IllegalArgumentError("Invalid update version " +
                    "[want=" + expVersion + ", got=" + update + "]");
            }
        }
        _updates.push(update);
    }

    /**
     * Returns all of the updates that should be applied to a scene with
     * the specified version to bring it up to date. <code>null</code> is
     * returned if the scene's version is older than the oldest update in
     * our list, in which case it cannot be brought up to date by applying
     * updates from this list.
     */
    public function getUpdates (fromVersion :int) :Array /*of SceneUpdate*/
    {
        if (_minVersion == -1 || fromVersion < _minVersion) {
            return null;
        }

        var offset :int = fromVersion - _minVersion;
        return _updates.slice(offset);
    }

    /**
     * Returns true if the supplied actual scene version is in accordance
     * with the updates contained in this list.
     */
    public function validate (sceneVersion :int) :Boolean
    {
        return ((_minVersion == -1) || // we have no updates
                (_minVersion + _updates.length == sceneVersion));
    }

    protected var _updates :Array;
    protected var _minVersion :int = -1;
}
}
