//
// $Id: SimpleMisoSceneImpl.java,v 1.1 2003/02/12 05:39:15 mdb Exp $

package com.threerings.miso.data;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Random;

import com.threerings.media.tile.TileUtil;

import com.threerings.miso.Log;
import com.threerings.miso.client.util.ObjectSet;

/**
 * A simple implementation of the {@link MisoScene} interface that assumes
 * a scene will be relatively small and all tile and object data can be
 * held in memory all at once.
 */
public class SimpleMisoSceneImpl
    implements MisoScene
{
    /**
     * Creates an initializes an instance using the supplied source model.
     */
    public SimpleMisoSceneImpl (SimpleMisoSceneModel model)
    {
        _model = model;

        // create display object infos for our uninteresting objects
        int ocount = (_model.objectTileIds == null) ? 0 :
            _model.objectTileIds.length;
        for (int ii = 0; ii < ocount; ii++) {
            _objects.add(createObjectInfo(_model.objectTileIds[ii],
                                          _model.objectXs[ii],
                                          _model.objectYs[ii]));
        }

        // create display object infos for our interesting objects
        for (int ii = 0, ll = _model.objectInfo.length; ii < ll; ii++) {
            _objects.add(createObjectInfo(_model.objectInfo[ii]));
        }
    }

    // documentation inherited from interface
    public int getBaseTileId (int x, int y)
    {
        return _model.getBaseTile(x, y);
    }

    // documentation inherited from interface
    public void getObjects (Rectangle region, ObjectSet set)
    {
        for (int ii = 0, ll = _objects.size(); ii < ll; ii++) {
            ObjectInfo info = (ObjectInfo)_objects.get(ii);
            if (region.contains(info.x, info.y)) {
                set.insert(info);
            }
        }
    }

    // documentation inherited from interface
    public void setBaseTile (int fqTileId, int x, int y)
    {
        _model.setBaseTile(x, y, fqTileId);
    }

    // documentation inherited from interface
    public void setBaseTiles (Rectangle r, int setId, int setSize)
    {
        for (int x = r.x; x < r.x + r.width; x++) {
            for (int y = r.y; y < r.y + r.height; y++) {
                int index = _rando.nextInt(setSize);
                setBaseTile(TileUtil.getFQTileId(setId, index), x, y);
            }
        }
    }

    // documentation inherited from interface
    public ObjectInfo addObject (int fqTileId, int x, int y)
    {
        ObjectInfo info = createObjectInfo(fqTileId, x, y);
        _model.addObject(info);
        _objects.add(info);
        return info;
    }

    // documentation inherited from interface
    public boolean removeObject (ObjectInfo info)
    {
        _model.removeObject(info);
        return _objects.remove(info);
    }

    // documentation inherited from interface
    public MisoSceneModel getSceneModel ()
    {
        return _model;
    }

    /**
     * Return a string representation of this Miso scene object.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer("[");
        toString(buf);
        return buf.append("]").toString();
    }

    /**
     * An extensible {@link #toString()} helper.
     */
    protected void toString (StringBuffer buf)
    {
        buf.append("width=").append(_model.width);
        buf.append(", height=").append(_model.height);
    }

    /**
     * Creates an {@link ObjectInfo} record from the supplied tile
     * information.
     */
    protected ObjectInfo createObjectInfo (int tileId, int x, int y)
    {
        return new ObjectInfo(tileId, x, y);
    }

    /**
     * Creates an {@link ObjectInfo} record from the supplied source
     * record.
     */
    protected ObjectInfo createObjectInfo (ObjectInfo source)
    {
        return source;
    }

    /** The miso scene model from which we obtain our data. */
    protected SimpleMisoSceneModel _model;

    /** The scene object records. */
    protected ArrayList _objects = new ArrayList();

    /** A random number generator for filling random base tiles. */
    protected Random _rando = new Random();
}
