//
// $Id: EditableSpotSceneModel.java,v 1.2 2001/12/05 03:38:09 mdb Exp $

package com.threerings.whirled.tools.spot;

import com.samskivert.util.StringUtil;

import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.tools.EditableSceneModel;

public class EditableSpotSceneModel extends EditableSceneModel
{
    /** The spot scene model that we extend with editable info. */
    public SpotSceneModel spotSceneModel;

    /** The names of the portals in this scene. */
    public String[] portalNames;

    /** The names of the portals in neighboring scenes to which our
     * portals link. */
    public String[] targetPortalNames;

    /**
     * Derived classes override this to tack their <code>toString</code>
     * data on to the string buffer.
     */
    protected void delegatesToString (StringBuffer buf)
    {
        buf.append(spotSceneModel);
    }

    /**
     * Derived classes override this to tack their <code>toString</code>
     * data on to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", portalNames=").
            append(StringUtil.toString(portalNames));
        buf.append(", targetPortalNames=").
            append(StringUtil.toString(targetPortalNames));
    }

    /**
     * Creates and returns a blank scene model.
     */
    public static EditableSpotSceneModel blankSpotSceneModel ()
    {
        EditableSpotSceneModel model = new EditableSpotSceneModel();
        model.spotSceneModel = SpotSceneModel.blankSpotSceneModel();
        model.sceneModel = model.spotSceneModel;
        populateBlankSpotSceneModel(model);
        return model;
    }

    /**
     * Populates a blank scene model with blank values.
     */
    protected static void populateBlankSpotSceneModel (
        EditableSpotSceneModel model)
    {
        // populate our superclass fields
        populateBlankSceneModel(model);

        // now populate our fields
        model.portalNames = new String[0];
        model.targetPortalNames = new String[0];
    }
}
