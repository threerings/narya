package com.threerings.crowd.chat.client {

import com.threerings.util.Long;

import com.threerings.presents.client.InvocationService_InvocationListener

public interface TellListener extends InvocationService_InvocationListener
{
    function tellSucceeded (idleTime :Long, awayMessage :String) :void;
}
}
