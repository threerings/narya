//
// $Id: AuthResponseData.java,v 1.2 2001/05/29 03:27:59 mdb Exp $

package com.samskivert.cocktail.cher.net;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

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

    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeUTF(code);
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        code = in.readUTF();
    }
}
