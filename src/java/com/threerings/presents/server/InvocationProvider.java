//
// $Id: InvocationProvider.java,v 1.3 2001/07/23 21:13:29 mdb Exp $

package com.threerings.cocktail.cher.server;

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
 *         public Object[] handleTestRequest (ClientObject source,
 *                                            String one, int two)
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
 * <code>createResponse</code> methods. These take a response identifier
 * (which determines the name of the method that will be invoked on the
 * response target object provided in the client) and a variable number of
 * arguments. If a response was created with the identifier
 * <code>TellFailed</code>, that would result in the method
 * <code>handleTellFailed</code> being invoked on the response target
 * object in the client. Again the arguments much match exactly and follow
 * the reflection rules for automatic conversion of primitive types
 * (supply an <code>Integer</code> object for <code>int</code> params,
 * etc.).
 */
public class InvocationProvider
{
    /**
     * Creates a response array properly configured with the supplied name
     * and no arguments.
     */
    protected Object[] createResponse (String name)
    {
        return new Object[] { name, null };
    }

    /**
     * Creates a response array properly configured with the supplied name
     * and single argument.
     */
    protected Object[] createResponse (String name, Object arg)
    {
        return new Object[] { name, null, arg };
    }

    /**
     * Creates a response array properly configured with the supplied name
     * and two arguments.
     */
    protected Object[] createResponse (String name, Object arg1, Object arg2)
    {
        return new Object[] { name, null, arg1, arg2 };
    }

    /**
     * Creates a response array properly configured with the supplied name
     * and three arguments.
     */
    protected Object[] createResponse (String name, Object arg1, Object arg2,
                                       Object arg3)
    {
        return new Object[] { name, null, arg1, arg2, arg3 };
    }

    /**
     * Creates a response array properly configured with the supplied name
     * and varying number of arguments.
     */
    protected Object[] createResponse (String name, Object[] args)
    {
        Object[] rsp = new Object[args.length+2];
        rsp[0] = name;
        System.arraycopy(args, 0, rsp, 2, args.length);
        return rsp;
    }
}
