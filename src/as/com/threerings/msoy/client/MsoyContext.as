package com.threerings.msoy.client {

import flash.display.DisplayObject;
import flash.display.Stage;

import mx.core.Application;

import com.threerings.util.MessageManager;

import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DObjectManager;

import com.threerings.crowd.client.ChatDirector;
import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.OccupantDirector;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.crowd.chat.client.ChatDirector;

public class MsoyContext
    implements CrowdContext
{
    public function MsoyContext (client :Client, app :Application)
    {
        _client = client;
        _app = app;

        // TODO: verify params to these constructors
        _msgmgr = new MessageManager("rsrc");
        _locdir = new LocationDirector(this);
        _chatdir = new ChatDirector(this, _msgmgr, "general");
    }

    // documentation inherited from superinterface PresentsContext
    public function getClient () :Client
    {
        return _client;
    }

    // documentation inherited from superinterface PresentsContext
    public function getDObjectManager () :DObjectManager
    {
        return _client.getDObjectManager();
    }

    // documentation inherited from interface CrowdContext
    public function getLocationDirector () :LocationDirector
    {
        return _locdir;
    }

    // documentation inherited from interface CrowdContext
    public function getOccupantDirector () :OccupantDirector
    {
        return null; // TODO
    }

    // documentation inherited from interface CrowdContext
    public function getChatDirector () :ChatDirector
    {
        return _chatdir;
    }

    // documentation inherited from interface CrowdContext
    public function setPlaceView (view :PlaceView) :void
    {
        for (var ii :int = _app.numChildren - 1; ii >= 0; ii--) {
            _app.removeChildAt(ii);
        }

        _app.addChild(view as DisplayObject);
    }

    // documentation inherited from interface CrowdContext
    public function clearPlaceView (view :PlaceView) :void
    {
        _app.removeChild(view as DisplayObject);
    }

    protected var _client :Client;

    protected var _app :Application;

    protected var _msgmgr :MessageManager;

    protected var _locdir :LocationDirector;

    protected var _chatdir :ChatDirector;
}
}
