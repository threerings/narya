//
// $Id: PuzzlerObject.java,v 1.3 2004/08/27 02:20:28 mdb Exp $
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

package com.threerings.puzzle.data;

/**
 * An interface that must be implemented by {@link BodyObject} derivations
 * that wish to be usable with the puzzle services.
 */
public interface PuzzlerObject
{
    /**
     * Returns this puzzler's "puzzle location which is the oid of the
     * puzzle game object. Should return -1 until some value is set via
     * {@link #setPuzzleLoc}.
     */
    public int getPuzzleLoc ();

    /**
     * Sets this puzzler's "puzzle location".
     */
    public void setPuzzleLoc (int puzzleLoc);
}
