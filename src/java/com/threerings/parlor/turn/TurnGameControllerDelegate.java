//
// $Id: TurnGameControllerDelegate.java,v 1.6 2004/08/27 02:20:15 mdb Exp $
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

package com.threerings.parlor.turn;

import com.threerings.util.Name;

import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.AttributeChangeListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.GameController;
import com.threerings.parlor.game.GameControllerDelegate;
import com.threerings.parlor.game.GameObject;

/**
 * Performs the client-side processing for a turn-based game. Games which
 * wish to make use of these services must construct a delegate and call
 * out to it at the appropriate times (see the method documentation for
 * which methods should be called when). The game's controller must also
 * implement the {@link TurnGameController} interface so that it can be
 * notified when turn-based game events take place.
 */
public class TurnGameControllerDelegate extends GameControllerDelegate
    implements AttributeChangeListener
{
    /**
     * Constructs a delegate which will call back to the supplied {@link
     * TurnGameController} implementation wen turn-based game related
     * things happen.
     */
    public TurnGameControllerDelegate (TurnGameController tgctrl)
    {
        super((GameController)tgctrl);

        // keep this around for later
        _tgctrl = tgctrl;
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
     * Returns the index of the current turn holder as configured in the
     * game object.
     *
     * @return the index into the players array of the current turn holder
     * or -1 if there is no current turn holder.
     */
    public int getTurnHolderIndex ()
    {
        return _gameObj.getPlayerIndex(_turnGame.getTurnHolder());
    }

    // documentation inherited
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        _ctx = ctx;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        // get a casted reference to the object
        _gameObj = (GameObject)plobj;
        _turnGame = (TurnGameObject)plobj;
        _thfield = _turnGame.getTurnHolderFieldName();

        // and add ourselves as a listener
        plobj.addListener(this);
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        // remove our listenership
        plobj.removeListener(this);

        // clean up
        _turnGame = null;
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        // handle turn changes
        if (event.getName().equals(_thfield)) {
            _tgctrl.turnDidChange((Name)event.getValue());
        }
    }

    /** The turn game controller for whom we are delegating. */
    protected TurnGameController _tgctrl;

    /** A reference to our client context. */
    protected CrowdContext _ctx;

    /** A reference to our game object. */
    protected GameObject _gameObj;

    /** A casted reference to our game object as a turn game. */
    protected TurnGameObject _turnGame;

    /** The name of the turn holder field. */
    protected String _thfield;
}
