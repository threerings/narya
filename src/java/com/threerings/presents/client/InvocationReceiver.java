//
// $Id: InvocationReceiver.java,v 1.5 2002/08/14 19:07:54 mdb Exp $

package com.threerings.presents.client;

import com.threerings.presents.dobj.DSet;

/**
 * Invocation notification receipt interfaces should be defined as
 * extending this interface. Actual notification receivers will implement
 * the requisite receiver interface definition and register themselves
 * with the {@link InvocationDirector} using the generated {@link
 * InvocationDispatcher} class specific to the notification receiver
 * interface in question. For example:
 *
 * <pre>
 * public class FooDirector implements FooReceiver
 * {
 *     public FooDirector (PresentsContext ctx)
 *     {
 *         InvocationDirector idir = ctx.getClient().getInvocationDirector();
 *         idir.registerReceiver(new FooDispatcher(this));
 *     }
 * }
 * </pre>
 *
 * @see InvocationDirector#registerReceiver
 */
public interface InvocationReceiver
{
    /**
     * Used to maintain a registry of invocation receivers that can be
     * used to convert (large) hash codes into (small) registration
     * numbers.
     */
    public static class Registration implements DSet.Entry
    {
        /** The unique hash code associated with this invocation receiver
         * class. */
        public String receiverCode;

        /** The unique id assigned to this invocation receiver class at
         * registration time. */
        public short receiverId;

        /** Creates and initializes a registration instance. */
        public Registration (String receiverCode, short receiverId)
        {
            this.receiverCode = receiverCode;
            this.receiverId = receiverId;
        }

        /** Creates a blank instance suitable for unserialization. */
        public Registration ()
        {
        }

        // documentation inherited from interface
        public Comparable getKey ()
        {
            return receiverCode;
        }

        /** Generates a string representation of this instance. */
        public String toString ()
        {
            return "[" + receiverCode + " => " + receiverId + "]";
        }
    }
}
