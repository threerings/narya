package com.threerings.crowd.chat.client {

import com.threerings.util.long;

import com.threerings.presents.client.InvocationListener

public interface TellListener extends InvocationListener
{
    function tellSucceeded (idleTime :long, awayMessage :String) :void;
}
}
