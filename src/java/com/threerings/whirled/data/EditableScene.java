//
// $Id: EditableScene.java,v 1.3 2001/10/05 23:59:36 mdb Exp $

package com.threerings.whirled.data;

/**
 * The editable scene interface. This separate interface allows the
 * ability to modify the scenes to be restricted only to code that needs
 * such an ability.
 */
public interface EditableScene extends Scene
{
    /**
     * Updates the scene's unique identifier.
     */
    public void setId (int sceneId);

    /**
     * Updates the scene's version.
     */
    public void setVersion (int version);
}
