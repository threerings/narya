//
// $Id: LobbyCodes.java,v 1.3 2001/10/11 04:13:33 mdb Exp $

package com.threerings.micasa.lobby;

import com.threerings.presents.client.InvocationCodes;

/**
 * Contains codes used by the lobby invocation services.
 */
public interface LobbyCodes extends InvocationCodes
{
    /** The module name for the lobby services. */
    public static final String MODULE_NAME = "lobby";

    /** The message identifier for a get categories request. */
    public static final String GET_CATEGORIES_REQUEST = "GetCategories";

    /** The message identifier for a got categories response. This is
     * mapped by the invocation services to a call to
     * <code>handleGotCategories</code>. */
    public static final String GOT_CATEGORIES_RESPONSE = "GotCategories";

    /** The message identifier for a get lobbies request. */
    public static final String GET_LOBBIES_REQUEST = "GetLobbies";

    /** The message identifier for a got lobbies response. This is mapped
     * by the invocation services to a call to
     * <code>handleGotLobbies</code>. */
    public static final String GOT_LOBBIES_RESPONSE = "GotLobbies";

    /** The message identifier for a failed request. This is
     * mapped by the invocation services to a call to
     * <code>handleRequestFailed</code>. */
    public static final String REQUEST_FAILED_RESPONSE = "RequestFailed";
}
