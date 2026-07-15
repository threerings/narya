package com.threerings.presents.peer.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.InvocationRequestEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.NamedEvent;
import com.threerings.presents.dobj.ProxySubscriber;
import com.threerings.presents.dobj.Subscriber;
import com.threerings.presents.server.PresentsObjectAccess;

import com.threerings.presents.peer.data.PeerClientObject;

import static com.threerings.presents.Log.log;

public enum NodeObjectAccess implements AccessController
{
    DEFAULT {
        @Override
        public boolean allowSubscribe (DObject object, Subscriber<?> subscriber)
        {
            return subscriber instanceof ProxySubscriber &&
              ((ProxySubscriber)subscriber).getClientObject() instanceof PeerClientObject;
        }
    },
    ;

    // from AccessController
    public boolean allowDispatch (DObject object, DEvent event)
    {
        return PresentsObjectAccess.DEFAULT.allowDispatch(object, event);
    }
}
