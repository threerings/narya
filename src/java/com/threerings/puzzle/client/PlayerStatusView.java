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

package com.threerings.puzzle.client;

import javax.swing.JPanel;

import com.threerings.util.Name;

import com.threerings.parlor.game.data.GameObject;

import com.threerings.puzzle.data.BoardSummary;
import com.threerings.puzzle.data.PuzzleConfig;
import com.threerings.puzzle.data.PuzzleObject;

/**
 * The player status view displays a player's current status in the game.
 */
public class PlayerStatusView extends JPanel
{
    /**
     * Constructs a player status view.
     */
    public PlayerStatusView (GameObject gameobj, int pidx)
    {
        // save off references
        _gameobj = gameobj;
        _username = _gameobj.players[pidx];
        _pidx = pidx;

        // configure the panel
        setOpaque(false);
    }

    /**
     * Initializes the player status view with the puzzle config.
     */
    public void init (PuzzleConfig config)
    {
        // nothing for now
    }

    /**
     * Get the player index of the player represented by this view.
     */
    public int getPlayerIndex ()
    {
        return _pidx;
    }

    /**
     * Sets the player board summary.
     */
    public void setBoardSummary (BoardSummary summary)
    {
        _summary = summary;
        repaint();
    }

    /**
     * Sets the player status.
     */
    public void setStatus (int status)
    {
        _status = status;
        repaint();
    }

    /**
     * Sets whether to highlight the player status display when rendered.
     */
    public void setHighlighted (boolean highlight)
    {
        _highlight = highlight;
        repaint();
    }

    /** Returns a string representation of this instance. */
    public String toString ()
    {
        return "[user=" + _username + ", pidx=" + _pidx +
            ", status=" + _status + "]";
    }

    /** The game object associated with this view. */
    protected GameObject _gameobj;

    /** The player name. */
    protected Name _username;

    /** The player index. */
    protected int _pidx;

    /** Whether this display is highlighted. */
    protected boolean _highlight;

    /** The player board summary. */
    protected BoardSummary _summary;

    /** The player game status. */
    protected int _status = PuzzleObject.PLAYER_IN_PLAY;
}
