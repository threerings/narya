//
// $Id: UpdateList.java,v 1.2 2003/06/11 04:14:11 mdb Exp $

package com.threerings.whirled.util;

import java.util.ArrayList;

import com.threerings.whirled.Log;
import com.threerings.whirled.data.SceneUpdate;

/**
 * A list specialized for storing {@link SceneUpdate} objects.
 */
public class UpdateList
{
    public UpdateList ()
    {
        _updates = new ArrayList();
    }

    /**
     * Adds an update to this list. The update must follow appropriately
     * the chain of updates established by the updates already in the list
     * (meaning it must operate on one version higher than the most recent
     * update already in the list).
     */
    public void addUpdate (SceneUpdate update)
    {
        if (_minVersion == -1) {
            // this is our first update, so we initialize our min version
            _minVersion = update.getSceneVersion();

        } else {
            int gotVersion = update.getSceneVersion();
            int expVersion = _minVersion + _updates.size();
            if (gotVersion > expVersion) {
                Log.warning("Update continuity broken, flushing list " +
                            "[got=" + update + ", expect=" + expVersion +
                            ", ucount=" + _updates.size() + "].");
                // flush out our old updates and start anew from here
                _updates.clear();
                _minVersion = expVersion;

            } else if (gotVersion < expVersion) {
                // we somehow got an update that's older than updates we
                // already have? wick wick wack
                String errmsg = "Invalid update version " +
                    "[want=" + expVersion + ", got=" + update + "]";
                throw new IllegalArgumentException(errmsg);
            }
        }
        _updates.add(update);
    }

    /**
     * Returns all of the updates that should be applied to a scene with
     * the specified version to bring it up to date. <code>null</code> is
     * returned if the scene's version is older than the oldest update in
     * our list, in which case it cannot be brought up to date by applying
     * updates from this list.
     */
    public SceneUpdate[] getUpdates (int fromVersion)
    {
        if (_minVersion == -1 || fromVersion < _minVersion) {
            return null;
        }

        int offset = fromVersion - _minVersion;
        int ucount = _updates.size() - offset;
        SceneUpdate[] updates = new SceneUpdate[ucount];
        for (int ii = 0; ii < ucount; ii++) {
            updates[ii] = (SceneUpdate)_updates.get(ii+offset);
        }
        return updates;
    }

    /**
     * Returns true if the supplied actual scene version is in accordance
     * with the updates contained in this list.
     */
    public boolean validate (int sceneVersion)
    {
        return ((_minVersion == -1) || // we have no updates
                (_minVersion + _updates.size() == sceneVersion));
    }

    protected ArrayList _updates;
    protected int _minVersion = -1;
}
