//
// $Id: SceneObjectActionEvent.java,v 1.2 2004/08/27 02:20:06 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
