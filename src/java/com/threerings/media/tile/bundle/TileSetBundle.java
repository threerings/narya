//
// $Id: TileSetBundle.java,v 1.14 2003/04/27 06:35:09 mdb Exp $

package com.threerings.media.tile.bundle;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Iterator;
import javax.imageio.ImageIO;

import com.samskivert.util.HashIntMap;

import com.threerings.resource.ResourceBundle;

import com.threerings.media.image.FastImageIO;
import com.threerings.media.image.ImageDataProvider;
import com.threerings.media.tile.TileSet;

/**
 * A tileset bundle is used to load up tilesets by id from a persistent
 * bundle of tilesets stored on the local filesystem.
 */
public class TileSetBundle extends HashIntMap
    implements Serializable, ImageDataProvider
{
    /**
     * Initializes this resource bundle with a reference to the jarfile
     * from which it was loaded and from which it can load image data. The
     * image manager will be used to decode the images.
     */
    public void init (ResourceBundle bundle)
    {
        _bundle = bundle;
    }

    /**
     * Returns the bundle file from which our tiles are fetched.
     */
    public File getSource ()
    {
        return _bundle.getSource();
    }

    /**
     * Adds a tileset to this tileset bundle.
     */
    public final void addTileSet (int tileSetId, TileSet set)
    {
        put(tileSetId, set);
    }

    /**
     * Retrieves a tileset from this tileset bundle.
     */
    public final TileSet getTileSet (int tileSetId)
    {
        return (TileSet)get(tileSetId);
    }

    /**
     * Enumerates the tileset ids in this tileset bundle.
     */
    public Iterator enumerateTileSetIds ()
    {
        return keySet().iterator();
    }

    /**
     * Enumerates the tilesets in this tileset bundle.
     */
    public Iterator enumerateTileSets ()
    {
        return values().iterator();
    }

    // documentation inherited from interface
    public String getIdent ()
    {
        return "tsb:" + _bundle.getSource();
    }

    // documentation inherited from interface
    public BufferedImage loadImage (String path)
        throws IOException
    {
        if (path.endsWith(FastImageIO.FILE_SUFFIX)) {
            return FastImageIO.read(_bundle.getResourceFile(path));
        } else {
            return ImageIO.read(_bundle.getResourceFile(path));
        }
    }

    // custom serialization process
    private void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(size());

        Iterator entries = entrySet().iterator();
        while (entries.hasNext()) {
            Entry entry = (Entry)entries.next();
            out.writeInt(((Integer)entry.getKey()).intValue());
            out.writeObject(entry.getValue());
        }
    }        

    // custom unserialization process
    private void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        int count = in.readInt();

        for (int i = 0; i < count; i++) {
            int tileSetId = in.readInt();
            TileSet set = (TileSet)in.readObject();
            put(tileSetId, set);
        }
    }

    /** That from which we load our tile images. */
    protected transient ResourceBundle _bundle;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 2;
 }
