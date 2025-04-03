//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2025 Three Rings Design, Inc., All Rights Reserved
// https://github.com/threerings/narya/blob/master/LICENSE

package com.threerings.presents.data;

/**
 * The invocation codes interface provides codes that are commonly used by invocation service
 * implementations. It is implemented as an interface so that were an invocation service to desire
 * to build on two or more other services, it can provide a codes interface that inherits from all
 * of the services that it extends.
 */
public interface InvocationCodes
{
    /** Defines a global invocation services group that can be used by clients and services that do
     * not care to make a distinction between groups of invocation services. */
    public static final String GLOBAL_GROUP = "presents";

    /** An error code returned to clients when a service cannot be performed because of some
     * internal server error that we couldn't explain in any meaningful way (things like null
     * pointer exceptions). */
    public static final String INTERNAL_ERROR = "m.internal_error";

    /** An error code returned to clients when a service cannot be performed because the requesting
     * client does not have the proper access. */
    public static final String ACCESS_DENIED = "m.access_denied";

    /** An error code returned to clients when a service cannot be performed because of some
     * internal server error that we couldn't explain in any meaningful way (things like null
     * pointer exceptions). */
    public static final String E_INTERNAL_ERROR = "e.internal_error";

    /** An error code returned to clients when a service cannot be performed because the requesting
     * client does not have the proper access. */
    public static final String E_ACCESS_DENIED = "e.access_denied";
}
