//
// $Id: AuthResponseData.java,v 1.7 2001/10/01 22:14:55 mdb Exp $

package com.threerings.cocktail.cher.net;

import com.threerings.cocktail.cher.dobj.DObject;

/**
 * An <code>AuthResponseData</code> object is communicated back to the
 * client along with an authentication response. It contains an indicator
 * of authentication success or failure along with bootstrap information
 * for the client.
 */
public class AuthResponseData extends DObject
{
    /** The constant used to indicate a successful authentication. */
    public static final String SUCCESS = "success";

    /**
     * Either the {@link #SUCCESS} constant or a reason code indicating
     * why the authentication failed.
     */
    public String code;

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        buf.append(", code=").append(code);
    }
}
