package com.threerings.presents.client {

import com.threerings.presents.client.InvocationDirector;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.ObjectAccessError;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.Log;

/**
 * This class is used by the InvocationDirector to subscribe
 * to the client object.
 */
public class ClientSubscriber implements Subscriber
{
    public function ClientSubscriber (invdir :InvocationDirector)
    {
        _invdir = invdir;
    }

    // documentation inherited from interface Subscriber
    public function objectAvailable (obj :DObject) :void
    {
        _invdir.gotClientObject(obj as ClientObject);
    }

    // documentation inherited from interface Subscriber
    public function requestFailed (oid :int, cause :ObjectAccessError) :void
    {
        _invdir.gotClientObjectFailed(oid, cause);
    }

    protected var _invdir :InvocationDirector;
}
}
