//
// $Id: AuthResponseData.java,v 1.10 2002/02/04 01:47:20 mdb Exp $

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
}
