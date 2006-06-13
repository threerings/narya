//
// $Id$
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

package com.threerings.cast;

/**
 * Thrown when an attempt is made to retrieve a non-existent character
 * component from the component repository.
 */
public class NoSuchComponentException extends Exception
{
    public NoSuchComponentException (int componentId)
    {
        super("No such component [componentId=" + componentId + "]");
        _componentId = componentId;
    }

    public NoSuchComponentException (
        String componentClass, String componentName)
    {
        super("No such component [class=" + componentClass +
              ", name=" + componentName + "]");
        _componentId = -1;
    }

    public int getComponentId ()
    {
        return _componentId;
    }

    protected int _componentId;
}
