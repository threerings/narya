//
// $Id: SceneObjectActionEvent.java,v 1.1 2003/06/19 22:20:02 mdb Exp $

package com.threerings.miso.client;

import java.awt.event.ActionEvent;

/**
 * An {@link ActionEvent} derivation that is fired when a scene object is
 * clicked or menu item selected.
 */
public class SceneObjectActionEvent extends ActionEvent
{
    public SceneObjectActionEvent (Object source, int id, String action,
                                   int modifiers, SceneObject scobj)
    {
        super(source, id, action, modifiers);
        _scobj = scobj;
    }

    /**
     * Returns the scene object that was the source of this action.
     */
    public SceneObject getSceneObject ()
    {
        return _scobj;
    }

    protected SceneObject _scobj;
}
