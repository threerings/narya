//
// $Id: PlayerStatusView.java,v 1.1 2003/11/26 01:42:34 mdb Exp $

package com.threerings.puzzle.client;

import javax.swing.JPanel;

import com.threerings.parlor.game.GameObject;

import com.threerings.puzzle.Log;
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
    protected String _username;

    /** The player index. */
    protected int _pidx;

    /** Whether this display is highlighted. */
    protected boolean _highlight;

    /** The player board summary. */
    protected BoardSummary _summary;

    /** The player game status. */
    protected int _status = PuzzleObject.PLAYER_IN_PLAY;
}
