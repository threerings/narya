//
// $Id: TileSetBundle.java,v 1.10 2002/09/23 18:19:57 mdb Exp $

package com.threerings.media.tile.bundle;

import java.awt.Image;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.util.Iterator;

import com.samskivert.util.HashIntMap;

import com.threerings.resource.ResourceBundle;
import com.threerings.media.ImageManager;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.ImageProvider;

/**
 * A tileset bundle is used to load up tilesets by id from a persistent
 * bundle of tilesets stored on the local filesystem.
 */
public class TileSetBundle extends HashIntMap
    implements Serializable, ImageProvider
{
    /**
     * Initializes this resource bundle with a reference to the jarfile
     * from which it was loaded and from which it can load image data. The
     * image manager will be used to decode the images.
     */
    public void init (ResourceBundle bundle, ImageManager imgr)
    {
        _bundle = bundle;
        _imgr = imgr;
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

    // documentation inherited
    public Image loadImage (String path)
        throws IOException
    {
        // obtain the image data from our jarfile
        InputStream imgin = _bundle.getResource(path);
        if (imgin == null) {
            String errmsg = "No such image in resource bundle " +
                "[bundle=" + _bundle + ", path=" + path + "].";
            throw new FileNotFoundException(errmsg);
        }
        // return _imgr.createImage(imgin);
        return ImageIO.read(imgin);
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
            set.setImageProvider(this);
            put(tileSetId, set);
        }
    }

    /** That from which we load our tile images. */
    protected ResourceBundle _bundle;

    /** We use the image manager to decode our images. */
    protected ImageManager _imgr;

    /** Increase this value when object's serialized state is impacted by
     * a class change (modification of fields, inheritance). */
    private static final long serialVersionUID = 1;
 }
