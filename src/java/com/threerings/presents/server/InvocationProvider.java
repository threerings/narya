//
// $Id: InvocationProvider.java,v 1.7 2002/04/17 18:20:04 mdb Exp $

package com.threerings.presents.server;

import com.samskivert.util.StringUtil;

import com.threerings.presents.Log;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationObject;
import com.threerings.presents.dobj.MessageEvent;

/**
 * Invocation providers should extend this class when implementing
 * invocation services. Because the service procedures are identified by
 * strings and the methods that are invoked are looked up via reflection,
 * the derived class doesn't override or implement any particular method.
 * However, the procedure names are still restricted. For example, a
 * procedure identified by the name <code>Tell</code> would result in the
 * invocation of a method named <code>handleTellRequest</code>. The
 * arguments to that method would be defined by the arguments that
 * accompanied the <code>Tell</code> invocation request along with the
 * client object of the client that made the request. Specifically:
 *
 * <pre>
 *     // client makes request
 *     Object[] args = new Object[] { "one", new Integer(2) };
 *     invmgr.invoke(MODULE, "Test", args, rsptarget);
 *
 *     // provider registered for MODULE should look like:
 *     public class TestProvider extends InvocationProvider
 *     {
 *         public void handleTestRequest (ClientObject source, int invid,
 *                                        String one, int two)
 *         {
 *             // ...
 *         }
 *     }
 * </pre>
 *
 * If the arguments do not match, a reflection error will happen when
 * trying to invoke the method and the whole request will fail.
 *
 * <p> Invocation procedures must also package up their response in a
 * particular way which is through the use of the
 * <code>sendResponse</code> methods. These take a response identifier
 * (which determines the name of the method that will be invoked on the
 * response target object provided in the client) and a variable number of
 * arguments. If a response was created with the identifier
 * <code>TellFailed</code>, that would result in the method
 * <code>handleTellFailed</code> being invoked on the response target
 * object in the client. Again the arguments much match exactly and follow
 * the reflection rules for automatic conversion of primitive types
 * (supply an <code>Integer</code> object for <code>int</code> params,
 * etc.).
 *
 * <p> Note that if an invocation service method throws a {@link
 * ServiceFailedException}, the invocation manager will automatically
 * issue a response to the client with the string <code>Failed</code>
 * appended to the request method. For example, if the client issues a
 * request for <code>Foo</code> which results in a call to
 * <code>handleFooRequest</code>, which throws a service failed exception,
 * the server will automatically issue a failure response named
 * <code>FooFailed</code> with the single argument being the
 * <code>reason</code> string provided to the constructor of the service
 * failed exception. The caller would then implement:
 *
 * <pre>
 * public void handleFooFailed (int invid, String reason)
 * </pre>
 *
 * to handle the failure.
 */
public class InvocationProvider
{
    /**
     * Delivers an invocation response properly configured with the
     * supplied name and no arguments.
     */
    protected void sendResponse (ClientObject source, int invid, String name)
    {
        deliverResponse(source, new Object[] { name, new Integer(invid) });
    }

    /**
     * Delivers an invocation response properly configured with the
     * supplied name and single argument.
     */
    protected void sendResponse (ClientObject source, int invid,
                                 String name, Object arg)
    {
        Object[] args = new Object[] { name, new Integer(invid), arg };
        deliverResponse(source, args);
    }

    /**
     * Delivers an invocation response properly configured with the
     * supplied name and two arguments.
     */
    protected void sendResponse (ClientObject source, int invid,
                                 String name, Object arg1, Object arg2)
    {
        Object[] args = new Object[] {
            name, new Integer(invid), arg1, arg2 };
        deliverResponse(source, args);
    }

    /**
     * Delivers an invocation response properly configured with the
     * supplied name and three arguments.
     */
    protected void sendResponse (ClientObject source, int invid,
                                 String name, Object arg1, Object arg2,
                                 Object arg3)
    {
        Object[] args = new Object[] {
            name, new Integer(invid), arg1, arg2, arg3 };
        deliverResponse(source, args);
    }

    /**
     * Delivers an invocation response properly configured with the
     * supplied name and varying number of arguments.
     */
    protected void sendResponse (ClientObject source, int invid,
                                 String name, Object[] args)
    {
        Object[] rargs = new Object[args.length+2];
        rargs[0] = name;
        rargs[1] = new Integer(invid);
        System.arraycopy(args, 0, rargs, 2, args.length);
        deliverResponse(source, rargs);
    }

    protected void deliverResponse (ClientObject source, Object[] args)
    {
        // make sure they didn't go away in the meanwhile
        if (source.isActive()) {
            // create the response event
            MessageEvent mevt = new MessageEvent(
                source.getOid(), InvocationObject.RESPONSE_NAME, args);
            // and ship it off
            PresentsServer.omgr.postEvent(mevt);

        } else {
            Log.warning("Dropping invrsp due to disappearing client " +
                        "[cloid=" + source.getOid() +
                        ", args=" + StringUtil.toString(args) + "].");
        }
    }
}
