//
// $Id: EditablePortal.java,v 1.1 2001/12/04 22:34:04 mdb Exp $

package com.threerings.whirled.tools.spot;

import com.threerings.whirled.spot.data.Portal;

/**
 * An editable portal contains a name as well as the standard portal
 * information.
 */
public class EditablePortal extends EditableLocation
{
    /** The portal whose data we extend. (My kingdom for multiple
     * inheritance.) */
    public Portal portal;

    /** The human-readable name of the scene to which this portal
     * links. */
    public String targetSceneName;

    /** The human-readable name of the location to which this portal links
     * in its target scene. */
    public String targetLocName;

    /**
     * Constructs an editable portal. A portal delegate will be created
     * with the supplied basic portal information.
     */
    public EditablePortal (
        int id, int x, int y, int orientation, int targetSceneId,
        int targetLocId, String name, String targetSceneName,
        String targetLocName)
    {
        this(new Portal(id, x, y, orientation, targetSceneId, targetLocId),
             name, targetSceneName, targetLocName);
    }

    /**
     * Constructs an editable portal with the specified portal delegate
     * and specified extended information.
     */
    public EditablePortal (Portal source, String name,
                           String targetSceneName, String targetLocName)
    {
        super(source, name);
        portal = source;
        this.targetSceneName = targetSceneName;
        this.targetLocName = targetLocName;
    }

    // documentation inherited
    protected void delegatesToString (StringBuffer buf)
    {
        buf.append(portal);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", targetScene=").append(targetSceneName);
        buf.append(", targetLoc=").append(targetLocName);
    }
}
