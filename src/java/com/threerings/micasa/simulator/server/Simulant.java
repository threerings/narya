//
// $Id: Simulant.java,v 1.3 2002/02/05 22:11:51 mdb Exp $

package com.threerings.micasa.simulator.client;

import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.GameCodes;
import com.threerings.parlor.game.GameConfig;

public abstract class Simulant implements GameCodes
{
    /**
     * Initializes the simulant with a body object and the game config for
     * the game they'll be engaged in.
     */
    public void init (BodyObject self, GameConfig config, DObjectManager omgr)
    {
        _self = self;
        _config = config;
        _omgr = omgr;
    }

    /**
     * Called when the simulant is about to enter the room in which it
     * will be doing all of its business.  Default implementation
     * immediately notifies the game manager that the simulant is ready to
     * play.  Sub-classes may wish to override this to do things like
     * subscribe to the game object, but should be sure to call this
     * method when they're finished to give the game manager the go-ahead
     * to proceed.
     */
    public void willEnterPlace (PlaceObject plobj)
    {
        // let the game manager know that the simulant's ready
        MessageEvent mevt = new MessageEvent(
            plobj.getOid(), PLAYER_READY_NOTIFICATION, null);
        postEvent(mevt);
    }

    /**
     * Posts the given message event to the server.  Since the simulant
     * resides within the server itself, it has no available client
     * distributed object manager and so we must set up the source oid
     * ourselves before sending it on its merry way.  Sub-classes should
     * accordingly be sure to make use of this method to send any
     * messages.
     */
    protected void postEvent (MessageEvent mevt)
    {
        mevt.setSourceOid(_self.getOid());
        _omgr.postEvent(mevt);
    }

    /** The game config object. */
    protected GameConfig _config;

    /** Our body object. */
    protected BodyObject _self;

    /** The object manager with which we're interacting. */
    protected DObjectManager _omgr;
}
