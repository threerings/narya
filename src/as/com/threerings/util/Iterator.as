//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.util {

/**
 * Java has Iterator, ActionScript has IViewCursor.
 * The problem is, IViewCursor defines 14 methods and 5 read-only properties.
 * That is a serious PITA to write for every collection that might desire
 * iteration. This provides a simpler alternative.
 */
public interface Iterator
{
    /**
     * Is there another element available?
     */
    function hasNext () :Boolean;

    /**
     * Returns the next element.
     */
    function next () :Object;

    /**
     * Remove the last returned element.
     */
    function remove () :void;
}
}
