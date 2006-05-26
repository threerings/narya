package com.threerings.presents.data {

import com.threerings.presents.client.Client;
import com.threerings.presents.client.GotTimeBaseListener;
import com.threerings.presents.client.TimeBaseService;

import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.presents.dobj.InvocationResponseEvent;

// TODO
// This will be auto-generated soon from the service definition
public class TimeBaseMarshaller extends InvocationMarshaller
    implements TimeBaseService
{
    public static const GET_TIME_OID :int = 1;

    public function getTimeOid (
            arg1 :Client, arg2 :String, arg3 :GotTimeBaseListener) :void
    {
        var listener3 :TimeBaseMarshaller_GotTimeBaseMarshaller = new TimeBaseMarshaller_GotTimeBaseMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, GET_TIME_OID, [arg2, listener3]);
    }
}
}
