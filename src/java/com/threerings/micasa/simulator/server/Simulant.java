//
// $Id: Simulant.java,v 1.5 2004/08/27 02:12:54 mdb Exp $
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

package com.threerings.micasa.simulator.server;

import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.GameConfig;
import com.threerings.parlor.game.GameManager;

public abstract class Simulant
{
    /**
     * Initializes the simulant with a body object and the game config for
     * the game they'll be engaged in.
     */
    public void init (BodyObject self, GameConfig config,
                      GameManager gmgr, DObjectManager omgr)
    {
        _self = self;
        _config = config;
        _gmgr = gmgr;
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
        _gmgr.playerReady(_self);
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

    /** The game manager for the game we're playing. */
    protected GameManager _gmgr;

    /** Our body object. */
    protected BodyObject _self;

    /** The object manager with which we're interacting. */
    protected DObjectManager _omgr;
}
