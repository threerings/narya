//
// $Id: EditableScene.java,v 1.1 2001/09/21 02:30:35 mdb Exp $

package com.threerings.whirled.data;

/**
 * The editable scene interface. This separate interface allows the
 * ability to modify the scenes to be restricted only to code that needs
 * such an ability.
 */
public interface EditableScene extends Scene
{
    /**
     * Updates the scene's name.
     */
    public void setName (String name);
}
