//
// $Id: InvocationCodes.java,v 1.1 2001/10/01 22:14:54 mdb Exp $

package com.threerings.cocktail.cher.client;

/**
 * The invocation codes interface provides codes that are commonly used by
 * invocation service implementations. It is implemented as an interface
 * so that were an invocation service to desire to build on two or more
 * other services, it can provide a codes interface that inherits from all
 * of the services that it extends.
 */
public interface InvocationCodes
{
    /**
     * Generally used in responses that can either have the value success,
     * or a string code explaining the reason for failure.
     */
    public static final String SUCCESS = "success";
}
