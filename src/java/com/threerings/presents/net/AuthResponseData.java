//
// $Id: AuthResponseData.java,v 1.1 2001/05/23 04:03:40 mdb Exp $

package com.samskivert.cocktail.cher.net;

import com.samskivert.cocktail.cher.dobj.DObject;

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
}
