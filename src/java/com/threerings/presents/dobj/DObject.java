//
// $Id: DObject.java,v 1.2 2001/05/23 04:03:40 mdb Exp $

package com.samskivert.cocktail.cher.dobj;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.samskivert.cocktail.cher.io.TypedObject;
import com.samskivert.cocktail.cher.io.TypedObjectFactory;

public class DObject implements TypedObject
{
    public static short TYPE = 400;

    public short getType ()
    {
        return TYPE;
    }

    public void writeTo (DataOutputStream out)
        throws IOException
    {
    }

    public void readFrom (DataInputStream in)
        throws IOException
    {
    }
}
