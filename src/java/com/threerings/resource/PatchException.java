//
// $Id: PatchException.java,v 1.1 2003/08/09 00:31:27 mdb Exp $

package com.threerings.resource;

import java.io.IOException;

/**
 * An exception thrown when we fail to patch one or more updated files.
 */
public class PatchException extends IOException
{
    public PatchException (String msg)
    {
        super(msg);
    }
}
