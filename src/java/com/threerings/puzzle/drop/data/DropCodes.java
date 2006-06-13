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

import com.threerings.puzzle.data.PuzzleGameCodes;

/**
 * Contains codes used by the drop game services.
 */
public interface DropCodes extends PuzzleGameCodes
{
    /** The message bundle identifier for drop puzzle messages. */
    public static final String DROP_MESSAGE_BUNDLE = "puzzle.drop";

    /** The name of the control stream that provides drop pieces. */
    public static final String DROP_STREAM = "drop";

    /** The name of the control stream that provides rise pieces. */
    public static final String RISE_STREAM = "rise";
}
