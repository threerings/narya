//
// $Id: InvocationManager.java,v 1.2 2001/07/19 07:09:16 mdb Exp $

package com.threerings.cocktail.cher.server;

import java.lang.reflect.Method;
import java.util.HashMap;

import com.threerings.cocktail.cher.Log;
import com.threerings.cocktail.cher.dobj.*;
import com.threerings.cocktail.cher.data.*;
import com.threerings.cocktail.cher.util.ClassUtil;

/**
 * The invocation services provide client to server invocations (service
 * requests) and server to client invocations (responses and
 * notifications). Via this mechanism, the client can make requests of the
 * server, be notified of its response and the server can asynchronously
 * invoke code on the client.
 *
 * <p> Invocations are like remote procedure calls in that they are named
 * and take arguments. They are simple in that the arguments can only be
 * of a small set of supported types (the set of distributed object field
 * types) and there is no special facility provided for referencing
 * non-local objects (it is assumed that the distributed object facility
 * will already be in use for any objects that should be shared).
 *
 * <p> The server invocation manager listens for invocation requests from
 * the client and passes them on to the invocation provider registered for
 * the requested invocation module. It also provides a mechanism by which
 * responses and asynchronous notification invocations can be delivered to
 * the client.
 */
public class InvocationManager
    implements Subscriber
{
    public InvocationManager (DObjectManager omgr)
    {
        _omgr = omgr;

        // create the object on which we'll listen for invocation requests
        omgr.createObject(DObject.class, this, true);
    }

    public int getOid ()
    {
        return _invoid;
    }

    /**
     * Registers the supplied invocation provider instance as the handler
     * for all invocation requests for the specified module.
     */
    public void registerProvider (String module, Object provider)
    {
        _providers.put(module, provider);
    }

    public void objectAvailable (DObject object)
    {
        // this must be our invocation object
        _invoid = object.getOid();
    }

    public void requestFailed (int oid, ObjectAccessException cause)
    {
        // if for some reason we were unable to create our invocation
        // object, we'll end up here
        Log.warning("Unable to create invocation object " +
                    "[reason=" + cause + "].");
        _invoid = -1;
    }

    public boolean handleEvent (DEvent event, DObject target)
    {
        // we shouldn't be getting non-message events, but check just to
        // be sure
        if (!(event instanceof MessageEvent)) {
            Log.warning("Got non-message event!? [evt=" + event + "].");
            return true;
        }

        // make sure the name is proper just for sanities sake
        MessageEvent mevt = (MessageEvent)event;
        if (!mevt.getName().equals(InvocationObject.MESSAGE_NAME)) {
            return true;
        }

        // we've got an invocation request, so we process it
        Object[] args = mevt.getArgs();
        String module = (String)args[0];
        String procedure = (String)args[1];
        int invid = ((Integer)args[2]).intValue();

        // locate a provider for this module
        Object provider = _providers.get(module);
        if (provider == null) {
            Log.warning("No provider registered for invocation request " +
                        "[evt=" + mevt + "].");
            return true;
        }

        // prune the method arguments from the full message arguments
        Object[] margs = new Object[args.length-3];
        System.arraycopy(args, 3, margs, 0, margs.length);

        // look up the method that will handle this procedure
        String mname = "handle" + procedure + "Request";
        Method procmeth = ClassUtil.getMethod(mname, provider, _methcache);
        if (procmeth == null) {
            Log.warning("Unable to resolve provider procedure " +
                        "[provider=" + provider.getClass().getName() +
                        ", method=" + mname + "].");
            return true;
        }

        // and invoke it
        try {
            procmeth.invoke(provider, margs);
        } catch (Exception e) {
            Log.warning("Error invoking invocation procedure " +
                        "[provider=" + provider + ", method=" + procmeth +
                        ", error=" + e + "].");
        }

        return true;
    }

    protected DObjectManager _omgr;
    protected int _invoid;
    protected HashMap _providers = new HashMap();
    protected HashMap _methcache = new HashMap();
}
