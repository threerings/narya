//
// $Id: AuthResponseData.java,v 1.6 2001/07/19 07:09:16 mdb Exp $

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
    /**
     * Either the string "success" or a reason code for why authentication
     * failed.
     */
    public String code;

    public String toString ()
    {
        return "[code=" + code + "]";
    }
}
