//
// $Id: EditablePortal.java,v 1.2 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.tools.spot;

import com.threerings.whirled.spot.data.Portal;

/**
 * An editable portal extends the standard portal with information needed
 * by the loader and editor.
 */
public class EditablePortal extends Portal
{
    /** The human-readable name of this portal. */
    public String name;

    /** The human-readable name of the scene to which this portal
     * links. */
    public String targetSceneName;

    /** The human-readable name of the portal to which this portal links
     * in its target scene. */
    public String targetPortalName;

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", name=").append(name);
        buf.append(", targetScene=").append(targetSceneName);
        buf.append(", targetPortal=").append(targetPortalName);
    }
}
