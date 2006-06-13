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

package com.threerings.puzzle.drop.data;

import com.threerings.util.DirectionCodes;

/**
 * The drop piece codes interface contains constants common to the drop
 * game package.
 */
public interface DropPieceCodes extends DirectionCodes
{
    /** The piece constant denoting an empty board piece. */
    public static final byte PIECE_NONE = -1;

    /** The number of pieces in a drop block. */
    public static final int DROP_BLOCK_PIECE_COUNT = 2;
}
