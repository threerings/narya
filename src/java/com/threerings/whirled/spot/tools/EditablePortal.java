//
// $Id: EditablePortal.java,v 1.3 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.spot.tools;

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
}
