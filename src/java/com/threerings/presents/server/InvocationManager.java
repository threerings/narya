//
// $Id: InvocationManager.java,v 1.6 2001/08/11 00:05:58 mdb Exp $

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
    public void registerProvider (String module, InvocationProvider provider)
    {
        _providers.put(module, provider);
    }

    /**
     * Delivers an invocation notification to the specified client. The
     * <code>module</code> argument selects which
     * <code>InvocationReceiver</code> will be invoked and the
     * <code>procedure</code> argument indicates which method will be
     * invoked on that receiver.
     *
     * <p> The method is constructed as follows: a procedure name of
     * <code>Tell</code> will result in a method call to
     * <code>handleTellNotification</code>. The arguments provided with
     * the notification define the necessary signature of that method,
     * according to the argument conversion rules defined by the
     * reflection services (<code>Integer</code> is converted to
     * <code>int</code>, etc.).
     */
    public void sendNotification (
        int cloid, String module, String procedure, Object[] args)
    {
        // package up the arguments
        int alength = (args != null) ? args.length : 0;
        Object[] nargs = new Object[alength + 2];
        nargs[0] = module;
        nargs[1] = procedure;
        if (args != null) {
            System.arraycopy(args, 0, nargs, 2, alength);
        }

        // construct a message event and deliver it
        MessageEvent nevt = new MessageEvent(
            cloid, InvocationObject.NOTIFICATION_NAME, nargs);
        CherServer.omgr.postEvent(nevt);
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

        // make sure the name is proper just for sanity's sake
        MessageEvent mevt = (MessageEvent)event;
        if (!mevt.getName().equals(InvocationObject.REQUEST_NAME)) {
            return true;
        }

        // we've got an invocation request, so we process it
        Object[] args = mevt.getArgs();
        String module = (String)args[0];
        String procedure = (String)args[1];
        Integer invid = (Integer)args[2];

        // locate a provider for this module
        InvocationProvider provider =
            (InvocationProvider)_providers.get(module);
        if (provider == null) {
            Log.warning("No provider registered for invocation request " +
                        "[evt=" + mevt + "].");
            return true;
        }

        // prune the method arguments from the full message arguments
        Object[] margs = new Object[args.length-1];
        int cloid = mevt.getSourceOid();
        margs[0] = CherServer.omgr.getObject(cloid);
        // make sure the client is still around
        if (margs[0] == null) {
            Log.warning("Client no longer around for invocation provider " +
                        "request [module=" + module +
                        ", proc=" + procedure + ", cloid=" + cloid + "].");
            return true;
        }
        System.arraycopy(args, 2, margs, 1, args.length-2);

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
