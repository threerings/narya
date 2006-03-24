package com.threerings.crowd.chat.client {

import com.threerings.util.Long;

import com.threerings.presents.client.InvocationListener

public interface TellListener extends InvocationListener
{
    function tellSucceeded (idleTime :Long, awayMessage :String) :void;
}
}
