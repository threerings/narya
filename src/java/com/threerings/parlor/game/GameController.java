//
// $Id: GameController.java,v 1.3 2001/10/06 01:36:19 mdb Exp $

package com.threerings.parlor.client;

import java.awt.event.ActionEvent;

import com.samskivert.swing.Controller;

import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.party.client.PlaceController;
import com.threerings.cocktail.party.data.PlaceConfig;
import com.threerings.cocktail.party.data.PlaceObject;
import com.threerings.cocktail.party.util.PartyContext;

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
public abstract class GameController
    extends PlaceController implements Subscriber
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
    public void init (PartyContext ctx, PlaceConfig config)
    {
        super.init(ctx, config);

        // cast our references
        _ctx = (ParlorContext)ctx;
        _config = (GameConfig)config;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // obtain a casted reference
        _gobj = (GameObject)plobj;

        // and add ourselves as a subscriber
        _gobj.addSubscriber(this);
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // unsubscribe from the game object
        _gobj.removeSubscriber(this);
        _gobj = null;
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
    public void objectAvailable (DObject object)
    {
        Log.warning("Got call to objectAvailable()?! " +
                    "[object=" + object + "].");
    }

    // documentation inherited
    public void requestFailed (int oid, ObjectAccessException cause)
    {
        Log.warning("Got call to requestFailed()?! " +
                    "[oid=" + oid + ", cause=" + cause + "].");
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
