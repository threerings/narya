//
// $Id: EditableScene.java,v 1.2 2001/09/28 01:46:10 mdb Exp $

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

    /**
     * Updates the scene's name.
     */
    public void setName (String name);
}
