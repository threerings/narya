//
// $Id: AuthResponseData.java,v 1.9 2001/10/11 04:07:53 mdb Exp $

package com.threerings.presents.net;

import com.threerings.presents.dobj.DObject;

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
        super.toString(buf);
        buf.append(", code=").append(code);
    }
}
