//
// $Id: GameController.java,v 1.1 2001/10/01 06:19:15 mdb Exp $

package com.threerings.parlor.client;

import java.awt.event.ActionEvent;

import com.samskivert.swing.Controller;

import com.threerings.cocktail.cher.dobj.*;

import com.threerings.parlor.Log;
import com.threerings.parlor.data.GameConfig;
import com.threerings.parlor.data.GameObject;
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
public class GameController extends Controller implements Subscriber
{
    /**
     * Initializes this game controller with the game configuration that
     * was established during the match making process. Derived classes
     * may want to override this method to initialize themselves with
     * game-specific configuration parameters but they should be sure to
     * call <code>super.init</code> in such cases.
     *
     * @param gameOid the object id of the game object for the game we are
     * intended to control.
     * @param config the configuration of the game we are intended to
     * control.
     */
    public void init (ParlorContext ctx, int gameOid, GameConfig config)
    {
        // keep a reference to our context
        _ctx = ctx;

        // subscribe to the game object
        _ctx.getDObjectManager().subscribeToObject(gameOid, this);

        // keep the config around for later
        _config = config;
    }

    /**
     * Handles basic game controller action events. Derived classes should
     * be sure to call <code>super.handleAction</code> for events they
     * don't specifically handle.
     */
    public boolean handleAction (ActionEvent action)
    {
        return false;
    }

    // documentation inherited
    public void objectAvailable (DObject object)
    {
        // keep a reference around to the game object
        _gobj = (GameObject)object;
    }

    // documentation inherited
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.warning("Unable to subscribe to game object!? [oid=" + oid +
                    ", cause=" + cause + "].");
    }

    // documentation inherited
    public boolean handleEvent (DEvent event, DObject target)
    {
        if (event instanceof AttributeChangedEvent) {
            AttributeChangedEvent ace = (AttributeChangedEvent)event;

            // deal with game state changes
            if (ace.getName().equals(GameObject.STATE)) {
                switch (ace.getIntValue()) {
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
                                ", state=" + ace.getIntValue() + "].");
                    break;
                }
            }
        }

        return true;
    }

    /**
     * Called when the game transitions to the <code>IN_PLAY</code>
     * state. This happens when all of the players have arrived and the
     * server starts the game.
     */
    protected void gameDidStart ()
    {
    }

    /**
     * Called when the game transitions to the <code>GAME_OVER</code>
     * state. This happens when the game reaches some end condition by
     * normal means (is not cancelled or aborted).
     */
    protected void gameDidEnd ()
    {
    }

    /**
     * Called when the game was cancelled for some reason.
     */
    protected void gameWasCancelled ()
    {
    }

    /** A reference to the active parlor context. */
    protected ParlorContext _ctx;

    /** Our game configuration information. */
    protected GameConfig _config;

    /** A reference to the game object for the game that we're
     * controlling. */
    protected GameObject _gobj;
}
