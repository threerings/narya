//
// $Id: EditableSceneModel.java,v 1.4 2001/12/12 19:06:15 mdb Exp $

package com.threerings.whirled.tools;

import java.util.ArrayList;
import com.samskivert.util.StringUtil;

import com.threerings.whirled.data.SceneModel;

/**
 * The editable scene model contains information above and beyond the
 * regular scene model that is necessary for editing and loading scenes.
 */
public class EditableSceneModel
{
    /** The scene model that we extend. */
    public SceneModel sceneModel;

    /** The human readable name of this scene's neighbors. */
    public ArrayList neighborNames;

    /**
     * Generates a string representation of this scene model.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        delegatesToString(buf);
        toString(buf);
        return buf.append("]").toString();
    }

    // documentation inherited
    public Object clone ()
        throws CloneNotSupportedException
    {
        return cloneWithDelegate((SceneModel)sceneModel.clone());
    }

    // documentation inherited
    protected Object cloneWithDelegate (SceneModel sceneModel)
        throws CloneNotSupportedException
    {
        EditableSceneModel esm = (EditableSceneModel)super.clone();
        esm.sceneModel = sceneModel;
        return esm;
    }

    /**
     * Derived classes override this to tack their <code>toString</code>
     * data on to the string buffer.
     */
    protected void delegatesToString (StringBuffer buf)
    {
        buf.append(sceneModel);
    }

    /**
     * Derived classes override this to tack their <code>toString</code>
     * data on to the string buffer.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append(", neighborNames=").
            append(StringUtil.toString(neighborNames));
    }

    /**
     * Creates and returns a blank editable scene model.
     */
    public static EditableSceneModel blankSceneModel ()
    {
        EditableSceneModel model = new EditableSceneModel();
        model.sceneModel = SceneModel.blankSceneModel();
        populateBlankSceneModel(model);
        return model;
    }

    /**
     * Populates a blank editable scene model with blank values.
     */
    protected static void populateBlankSceneModel (EditableSceneModel model)
    {
        model.neighborNames = new ArrayList();
    }
}
