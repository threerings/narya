//
// $Id: PuzzlePanel.java,v 1.9 2004/10/29 00:48:37 eric Exp $
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

import java.awt.BorderLayout;
import javax.swing.JPanel;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.util.RuntimeAdjust;

import com.threerings.util.KeyTranslator;
import com.threerings.util.RobotPlayer;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.puzzle.Log;
import com.threerings.puzzle.data.PuzzleCodes;
import com.threerings.puzzle.data.PuzzleConfig;
import com.threerings.puzzle.data.PuzzleGameCodes;
import com.threerings.puzzle.util.PuzzleContext;

/**
 * The puzzle panel class should be extended by classes that provide a
 * view for a puzzle game.  The {@link PuzzleController} calls these
 * methods as necessary to perform its duties in managing the logical
 * actions of a puzzle game.
 */
public abstract class PuzzlePanel extends JPanel
    implements PlaceView, ControllerProvider, PuzzleCodes, PuzzleGameCodes
{
    /**
     * Constructs a puzzle panel.
     */
    public PuzzlePanel (PuzzleContext ctx, PuzzleController controller)
    {
        _ctx = ctx;
        _controller = controller;

        // set up the keyboard manager
        _xlate = getKeyTranslator();
        _ctx.getKeyboardManager().setTarget(this, _xlate);

        // configure the puzzle panel
        setLayout(new BorderLayout());

        // create the puzzle board view
        _bview = createBoardView(ctx);

        // create the puzzle board panel
        _bpanel = createBoardPanel(ctx);

        // add the board panel
        add(_bpanel, BorderLayout.CENTER);
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();

        // leave the keyboard manager disabled to start, and set things up
        // for chatting
        setPuzzleGrabsKeys(false);
    }

    /**
     * Temporarily replaces the puzzle board display with the supplied
     * overlay panel.  The panel can be removed and the board display
     * restored by calling {@link #popOverlayPanel}.
     *
     * @return true if the specified panel will be displayed, false if it
     * was not able to be pushed because another overlay panel is already
     * pushed onto the primary panel.
     */
    public boolean pushOverlayPanel (JPanel opanel)
    {
        // bail if we've already got an overlay
        if (_opanel != null) {
            Log.info("Refusing to push overlay panel, we've already got one " +
                     "[opanel=" + _opanel + ", npanel=" + opanel + "].");
            return false;
        }

        // swap in the overlay panel
        _opanel = opanel;
        remove(_bpanel);
        add(_opanel);

        // make sure the UI updates
        SwingUtil.refresh(this);

        return true;
    }

    /**
     * Pops the overlay panel off of the main puzzle board display.
     */
    public void popOverlayPanel ()
    {
        if (_opanel != null) {
            remove(_opanel);
            _opanel = null;
            add(_bpanel);

            // make sure the UI updates
            SwingUtil.refresh(this);
        }
    }

    /**
     * Returns true if an overlay panel is currently being displayed.
     */
    public boolean hasOverlay ()
    {
        return _opanel != null;
    }

    /**
     * Initializes the puzzle panel with the puzzle config of the puzzle
     * whose user interface is being displayed by the panel
     */
    public void init (PuzzleConfig config)
    {
        _config = config;
        _bview.init(config);
    }

    /**
     * Sets whether this panel receives events periodically from a robot
     * player.
     */
    public void setRobotPlayer (boolean isrobot)
    {
        // create a robot player if necessary
        if (_robot == null) {
            _robot = createRobotPlayer();
        }
        setPuzzleGrabsKeys(!isrobot);
        _robot.setRobotDelay(200L);
        _robot.setActive(isrobot);
    }

    /**
     * Sets whether the puzzle grabs keys or if they should go to the chat
     * window.
     */
    public void setPuzzleGrabsKeys (boolean puzgrabs)
    {
        // enable or disable the key manager appropriately
        _ctx.getKeyboardManager().setEnabled(puzgrabs);
        if (puzgrabs) {
            getBoardView().requestFocus();
        }
    }

    /**
     * Called by the controller when the action starts.
     */
    public void startAction ()
    {
        // make the first player a robot player

        if (isRobotTesting() && _controller.getPlayerIndex() == 0) {
            setRobotPlayer(true);
        }
    }

    /**
     * Called by the controller when the action stops.
     */
    public void clearAction ()
    {
        // deactivate the robot player
        if (isRobotTesting() && _controller.getPlayerIndex() == 0) {
            setRobotPlayer(false);
        }
    }

    /**
     * Creates a robot player using the default RobotPlayer.  Derived
     * classes can override to make use of more advanced robots adapted to
     * specific puzzles.
     */
    protected RobotPlayer createRobotPlayer ()
    {
        return new RobotPlayer(this, _xlate);
    }

    /**
     * Creates the puzzle board view that will be used to display the main
     * puzzle interface. This is called when the puzzle panel is
     * constructed. The derived panel will still be responsible for adding
     * the board to the interface hierarchy.
     */
    protected abstract PuzzleBoardView createBoardView (PuzzleContext ctx);

    /**
     * Creates the main panel used to display the puzzle and its various
     * in-game accoutrements (next block views, player status displays,
     * etc.)  This is called when the puzzle panel is constructed.  The
     * derived panel is responsible for making sure that the board view is
     * present in the board panel.
     */
    protected abstract JPanel createBoardPanel (PuzzleContext ctx);

    /**
     * Returns a key translator with the desired key to controller command
     * mappings desired for this puzzle.
     */
    protected abstract KeyTranslator getKeyTranslator ();

    // documentation inherited from interface
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    // documentation inherited from interface
    public void didLeavePlace (PlaceObject plobj)
    {
        // disable the keyboard manager when we leave
        _ctx.getKeyboardManager().reset();
    }

    /**
     * Returns a reference to the {@link PuzzleBoardView} in use.
     */
    public PuzzleBoardView getBoardView ()
    {
        return _bview;
    }

    // documentation inherited
    public Controller getController ()
    {
        return _controller;
    }

    /** Returns true if the robot tester is activated. */
    public static boolean isRobotTesting ()
    {
        return _robotTest.getValue();
    }

    /** Returns true if board syncing is activated. */
    public static boolean isSyncingBoards ()
    {
        return _syncBoardState.getValue();
    }

    /** Our puzzle context. */
    protected PuzzleContext _ctx;

    /** The board view on which the primary puzzle interface is displayed. */
    protected PuzzleBoardView _bview;

    /** The board panel displayed while the puzzle is in progress. */
    protected JPanel _bpanel;

    /** The board overlay panel displayed as requested. */
    protected JPanel _opanel;

    /** The puzzle config. */
    protected PuzzleConfig _config;

    /** The robot player. */
    protected RobotPlayer _robot;

    /** Our key translations. */
    protected KeyTranslator _xlate;

    /** The puzzle game controller. */
    protected PuzzleController _controller;

    /** A debug hook that toggles activation of the robot test player. */
    protected static RuntimeAdjust.BooleanAdjust _robotTest =
        new RuntimeAdjust.BooleanAdjust(
            "Activates the robot test player which will make random moves " +
            "in any activated puzzle.", "narya.puzzle.robot_tester",
            PuzzlePrefs.config, false);

    /** A debug hook that toggles activation of board syncing. */
    protected static RuntimeAdjust.BooleanAdjust _syncBoardState =
        new RuntimeAdjust.BooleanAdjust(
            "Sends a snapshot of the puzzle board with every event to aid " +
            "in debugging.", "narya.puzzle.sync_board_state",
            PuzzlePrefs.config, false);
}
