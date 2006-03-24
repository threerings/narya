package com.threerings.msoy.client {

import flash.util.describeType;

import mx.collections.ArrayCollection;

import com.threerings.util.Name;
import com.threerings.presents.Log;
import com.threerings.presents.client.Client;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.QSet;
import com.threerings.presents.net.UsernamePasswordCreds;

import com.threerings.presents.dobj.DObjectManager;

import com.threerings.presents.net.BootstrapData;

// imported so that they'll be compiled into the .swf
import com.threerings.presents.data.TimeBaseMarshaller;
import com.threerings.crowd.data.BodyMarshaller;
import com.threerings.crowd.data.LocationMarshaller;
import com.threerings.crowd.chat.data.ChatMarshaller;


public class MsoyClient extends Client
{
    public function MsoyClient ()
    {
        super(new UsernamePasswordCreds(new Name("guest"), "guest"));

        _ctx = new MsoyContext(this);

        setServer("tasman.sea.earth.threerings.net", DEFAULT_SERVER_PORT);
        logon();
    }

    public function fuckingCompiler () :void
    {
        var i :int = TimeBaseMarshaller.GET_TIME_OID;
        i = LocationMarshaller.LEAVE_PLACE;
        i = BodyMarshaller.SET_IDLE;
        i = ChatMarshaller.AWAY;
    }

    protected var _ctx :MsoyContext;
}
}
