//
// $Id: AuxModel.java,v 1.1 2003/02/12 07:23:31 mdb Exp $

package com.threerings.whirled.data;

import com.threerings.io.Streamable;

/**
 * An interface that must be implemented by auxiliary scene models.
 */
public interface AuxModel extends Streamable, Cloneable
{
    /**
     * Creates a clone of this auxiliary model.
     */
    public Object clone ()
        throws CloneNotSupportedException;
}
