//
// $Id: TurnGameControllerDelegate.java,v 1.1 2002/02/12 06:57:30 mdb Exp $

package com.threerings.parlor.turn;

import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.AttributeChangeListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.GameController;
import com.threerings.parlor.game.GameObject;

/**
 * Performs the client-side processing for a turn-based game. Games which
 * wish to make use of these services must construct a delegate and call
 * out to it at the appropriate times (see the method documentation for
 * which methods should be called when). The game's controller must also
 * implement the {@link TurnGameController} interface so that it can be
 * notified when turn-based game events take place.
 */
public class TurnGameControllerDelegate
    implements AttributeChangeListener
{
    /**
     * Constructs a delegate which will call back to the supplied {@link
     * TurnGameController} implementation wen turn-based game related
     * things happen.
     */
    public TurnGameControllerDelegate (TurnGameController tgctrl)
    {
        _tgctrl = tgctrl;
    }

    /**
     * This must be called from {@link GameController#init}.
     */
    public void init (CrowdContext ctx)
    {
        _ctx = ctx;
    }

    /**
     * Returns true if the game is in progress and it is our turn; false
     * otherwise.
     */
    public boolean isOurTurn ()
    {
        BodyObject self = (BodyObject)_ctx.getClient().getClientObject();
        return (_gameObj.state == GameObject.IN_PLAY &&
                _turnGame.getTurnHolder().equals(self.username));
    }

    /**
     * This must be called from {@link GameController#willEnterPlace}.
     */
    public void willEnterPlace (GameObject gobj)
    {
        // get a casted reference to the object
        _gameObj = gobj;
        _turnGame = (TurnGameObject)gobj;
        _thfield = _turnGame.getTurnHolderFieldName();

        // and add ourselves as a listener
        gobj.addListener(this);
    }

    /**
     * This must be called from {@link GameController#didLeavePlace}.
     */
    public void didLeavePlace (GameObject gobj)
    {
        // remove our listenership
        gobj.removeListener(this);

        // clean up
        _turnGame = null;
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        // handle turn changes
        if (event.getName().equals(_thfield)) {
            _tgctrl.turnDidChange((String)event.getValue());
        }
    }

    /** A reference to our client context. */
    protected CrowdContext _ctx;

    /** The turn game controller for whom we are delegating. */
    protected TurnGameController _tgctrl;

    /** A reference to our game object. */
    protected GameObject _gameObj;

    /** A casted reference to our game object as a turn game. */
    protected TurnGameObject _turnGame;

    /** The name of the turn holder field. */
    protected String _thfield;
}
