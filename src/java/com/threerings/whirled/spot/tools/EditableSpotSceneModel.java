//
// $Id: EditableSpotSceneModel.java,v 1.1 2001/12/04 22:34:04 mdb Exp $

package com.threerings.whirled.tools.spot;

import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.tools.EditableSceneModel;

public class EditableSpotSceneModel extends EditableSceneModel
{
    /** The spot scene model that we extend with editable info. */
    public SpotSceneModel spotSceneModel;

    /** The names of the locations in this scene. */
    public String[] locationNames;

    /** The names of the locations in neighboring scenes to which our
     * portals link. */
    public String[] targetLocNames;
}
