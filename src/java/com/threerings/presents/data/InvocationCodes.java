//
// $Id: InvocationCodes.java,v 1.1 2002/04/15 16:28:03 shaper Exp $

package com.threerings.presents.data;

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

    /** An error code returned to clients when a service cannot be
     * performed because the requesting client does not have the proper
     * access. */
    public static final String ACCESS_DENIED = "m.access_denied";
}
