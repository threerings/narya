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
 * Provides a mechanism where by a character component can obtain access
 * to its image frames for a particular action in an on demand manner.
 */
public interface FrameProvider
{
    /**
     * Returns the animation frames (in the eight sprite directions) for
     * the specified action of the specified component. May return null if
     * the specified action does not exist for the specified component.
     */
    public ActionFrames getFrames (
        CharacterComponent component, String action, String type);
}
