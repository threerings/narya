//
// $Id: GameController.java,v 1.11 2002/04/14 02:33:16 mdb Exp $

package com.threerings.parlor.game;

import java.awt.event.ActionEvent;

import com.samskivert.swing.Controller;

import com.threerings.presents.dobj.*;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.client.PlaceControllerDelegate;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.Log;
import com.threerings.parlor.util.ParlorContext;

/**
 * The game controller manages the flow and control of a game on the
 * client side. This class serves as the root of a hierarchy of controller
 * classes that aim to provide functionality shared between various
 * similar games. The base controller provides functionality for starting
 * and ending the game and for calculating ratings adjustements when a
 * game ends normally. It also handles the basic house keeping like
 * subscription to the game object and dispatch of commands and
 * distributed object events.
 */
public abstract class GameController extends PlaceController
    implements AttributeChangeListener, GameCodes
{
    /**
     * Initializes this game controller with the game configuration that
     * was established during the match making process. Derived classes
     * may want to override this method to initialize themselves with
     * game-specific configuration parameters but they should be sure to
     * call <code>super.init</code> in such cases.
     *
     * @param ctx the client context.
     * @param config the configuration of the game we are intended to
     * control.
     */
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        // cast our references before we call super.init() so that when
        // super.init() calls createPlaceView(), we have our casted
        // references already in place
        _ctx = (ParlorContext)ctx;
        _config = (GameConfig)config;

        super.init(ctx, config);
    }

    /**
     * Adds this controller as a listener to the game object (thus derived
     * classes need not do so) and lets the game manager know that we are
     * now ready to go.
     */
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // obtain a casted reference
        _gobj = (GameObject)plobj;

        // and add ourselves as a listener
        _gobj.addListener(this);

        // finally let the game manager know that we're ready to roll
        MessageEvent mevt = new MessageEvent(
            _gobj.getOid(), PLAYER_READY_NOTIFICATION, null);
        _ctx.getDObjectManager().postEvent(mevt);
    }

    /**
     * Removes our listener registration from the game object and cleans
     * house.
     */
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);

        // unlisten to the game object
        _gobj.removeListener(this);
        _gobj = null;
    }

    /**
     * Returns whether the game is over.
     */
    public boolean isGameOver ()
    {
        return (_gameOver || _gobj.state == GameObject.GAME_OVER);
    }

    /**
     * Sets the client game over override. This is used in situations
     * where we determine that the game is over before the server has
     * informed us of such.
     */
    public void setGameOver (boolean gameOver)
    {
        _gameOver = gameOver;
    }

    /**
     * Calls {@link #gameWillReset}, ends the current game (locally, it
     * does not tell the server to end the game), and waits to receive a
     * reset notification (which is simply an event setting the game state
     * to <code>IN_PLAY</code> even though it's already set to
     * <code>IN_PLAY</code>) from the server which will start up a new
     * game.  Derived classes should override {@link #gameWillReset} to
     * perform any game-specific animations.
     */
    public void resetGame ()
    {
        // let derived classes do their thing
        gameWillReset();

        // end the game until we receive a new board
        setGameOver(true);
    }

    /**
     * Handles basic game controller action events. Derived classes should
     * be sure to call <code>super.handleAction</code> for events they
     * don't specifically handle.
     */
    public boolean handleAction (ActionEvent action)
    {
        return super.handleAction(action);
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        // deal with game state changes
        if (event.getName().equals(GameObject.STATE)) {
            switch (event.getIntValue()) {
            case GameObject.IN_PLAY:
                gameDidStart();
                break;
            case GameObject.GAME_OVER:
                gameDidEnd();
                break;
            case GameObject.CANCELLED:
                gameWasCancelled();
                break;
            default:
                Log.warning("Game transitioned to unknown state " +
                            "[gobj=" + _gobj +
                            ", state=" + event.getIntValue() + "].");
                break;
            }
        }
    }

    /**
     * Called when the game transitions to the <code>IN_PLAY</code>
     * state. This happens when all of the players have arrived and the
     * server starts the game.
     */
    protected void gameDidStart ()
    {
        // clear out our game over flag
        setGameOver(false);

        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                ((GameControllerDelegate)delegate).gameDidStart();
            }
        });
    }

    /**
     * Called when the game transitions to the <code>GAME_OVER</code>
     * state. This happens when the game reaches some end condition by
     * normal means (is not cancelled or aborted).
     */
    protected void gameDidEnd ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                ((GameControllerDelegate)delegate).gameDidEnd();
            }
        });
    }

    /**
     * Called when the game was cancelled for some reason.
     */
    protected void gameWasCancelled ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                ((GameControllerDelegate)delegate).gameWasCancelled();
            }
        });
    }

    /**
     * Called to give derived classes a chance to display animations, send
     * a final packet, or do any other business they care to do when the
     * game is about to reset.
     */
    protected void gameWillReset ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp() {
            public void apply (PlaceControllerDelegate delegate) {
                ((GameControllerDelegate)delegate).gameWillReset();
            }
        });
    }

    /** A reference to the active parlor context. */
    protected ParlorContext _ctx;

    /** Our game configuration information. */
    protected GameConfig _config;

    /** A reference to the game object for the game that we're
     * controlling. */
    protected GameObject _gobj;

    /** A local flag overriding the game over state for situations where
     * the client knows the game is over before the server has
     * transitioned the game object accordingly. */
    protected boolean _gameOver;
}
