//
// $Id: LobbyCodes.java,v 1.1 2001/10/04 00:29:07 mdb Exp $

package com.threerings.micasa.lobdy;

import com.threerings.cocktail.cher.client.InvocationCodes;

/**
 * Contains codes used by the lobby invocation services.
 */
public interface LobbyCodes extends InvocationCodes
{
    /** The module name for the lobby services. */
    public static final String MODULE_NAME = "lobby";

    /** The message identifier for a get lobbies request. */
    public static final String GET_LOBBIES_REQUEST = "GetLobbies";
}
