//
// $Id: AuthResponseData.java,v 1.5 2001/07/19 05:56:20 mdb Exp $

package com.threerings.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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

    /** The oid of this client's associated distributed object. */
    public int clientOid;

    /** The oid to which to send invocation requests. */
    public int invOid;

    public String toString ()
    {
        return "[code=" + code + "]";
    }
}
