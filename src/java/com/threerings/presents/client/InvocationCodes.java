//
// $Id: InvocationCodes.java,v 1.3 2001/10/19 18:03:06 mdb Exp $

package com.threerings.presents.client;

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

    /** An error code returned to clients when a service cannot be
     * performed because of some internal server error that we couldn't
     * explain in any meaningful way (things like null pointer
     * exceptions). */
    public static final String INTERNAL_ERROR = "m.internal_error";
}
