//
// $Id: VirtualMisoSceneModel.java,v 1.6 2004/08/27 02:20:06 mdb Exp $
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

package com.threerings.miso.data;

/**
 * A convenient base class for "virtual" scenes which do not allow editing
 * and compute the base and object tiles rather than obtain them from some
 * data structure.
 */
public abstract class VirtualMisoSceneModel extends MisoSceneModel
{
    public VirtualMisoSceneModel ()
    {
    }

    // documentation inherited from interface
    public boolean setBaseTile (int fqTileId, int x, int y)
    {
        throw new UnsupportedOperationException();
    }

    // documentation inherited from interface
    public boolean addObject (ObjectInfo info)
    {
        throw new UnsupportedOperationException();
    }

    // documentation inherited from interface
    public void updateObject (ObjectInfo info)
    {
        throw new UnsupportedOperationException();
    }

    // documentation inherited from interface
    public boolean removeObject (ObjectInfo info)
    {
        throw new UnsupportedOperationException();
    }
}
