//
// $Id: MisoSceneModel.java,v 1.12 2003/02/12 05:39:15 mdb Exp $

package com.threerings.miso.data;

import com.threerings.io.SimpleStreamableObject;

/**
 * Contains basic information for a miso scene model that is shared among
 * every specialized model implementation.
 */
public class MisoSceneModel extends SimpleStreamableObject
    implements Cloneable
{
    /** The sector of our containing scene for which we contain model
     * data. Simple scenes may contain only one sector, but larger more
     * complicated scenes will likely contain multiple sectors to support
     * fine grain updates. */
    public int sectorId;

    /** The width of our sector, in tile units. */
    public int width;

    /** The height of our sector, in tile units. */
    public int height;

    /**
     * Creates a completely uninitialized model suitable for little more
     * than unserialization.
     */
    public MisoSceneModel ()
    {
    }

    /**
     * Creates a blank model with the specified dimensions.
     */
    public MisoSceneModel (int width, int height)
    {
        this.width = width;
        this.height = height;
    }

    /**
     * Creates a copy of this scene model.
     */
    public Object clone ()
    {
        try {
            return (MisoSceneModel)super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new RuntimeException(
                "MisoSceneModel.clone() booched " + cnse);
        }
    }
}
