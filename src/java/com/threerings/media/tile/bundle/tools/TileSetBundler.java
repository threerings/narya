//
// $Id: TileSetBundler.java,v 1.17 2003/06/17 23:29:33 ray Exp $

package com.threerings.media.tile.bundle.tools;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.Iterator;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.Deflater;

import org.xml.sax.SAXException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;

import org.apache.commons.io.StreamUtils;

import com.samskivert.io.NestableIOException;
import com.samskivert.io.PersistenceException;

import com.threerings.media.Log;
import com.threerings.media.image.Colorization;
import com.threerings.media.image.FastImageIO;
import com.threerings.media.image.ImageUtil;
import com.threerings.media.image.Mirage;

import com.threerings.media.tile.ObjectTileSet;
import com.threerings.media.tile.SimpleCachingImageProvider;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileSetIDBroker;
import com.threerings.media.tile.TrimmedObjectTileSet;
import com.threerings.media.tile.UniformTileSet;

import com.threerings.media.tile.bundle.BundleUtil;
import com.threerings.media.tile.bundle.TileSetBundle;
import com.threerings.media.tile.tools.xml.TileSetRuleSet;

/**
 * The tileset bundler is used to create tileset bundles from a set of XML
 * tileset descriptions in a bundle description file. The bundles contain
 * a serialized representation of the tileset objects along with the
 * actual image files referenced by those tilesets.
 *
 * <p> The organization of the bundle description file is customizable
 * based on the an XML configuration file provided to the tileset bundler
 * when constructed.  The bundler configuration maps XML paths to tileset
 * parsers. An example configuration follows:
 *
 * <pre>
 * &lt;bundler-config&gt;
 *   &lt;mapping&gt;
 *     &lt;path&gt;bundle.tilesets.uniform&lt;/path&gt;
 *     &lt;ruleset&gt;
 *       com.threerings.media.tile.tools.xml.UniformTileSetRuleSet
 *     &lt;/ruleset&gt;
 *   &lt;/mapping&gt;
 *   &lt;mapping&gt;
 *     &lt;path&gt;bundle.tilesets.object&lt;/path&gt;
 *     &lt;ruleset&gt;
 *       com.threerings.media.tile.tools.xml.ObjectTileSetRuleSet
 *     &lt;/ruleset&gt;
 *   &lt;/mapping&gt;
 * &lt;/bundler-config&gt;
 * </pre>
 *
 * This configuration would be used to parse a bundle description that
 * looked something like the following:
 *
 * <pre>
 * &lt;bundle&gt;
 *   &lt;tilesets&gt;
 *     &lt;uniform&gt;
 *       &lt;tileset&gt;
 *         &lt;!-- ... --&gt;
 *       &lt;/tileset&gt;
 *     &lt;/uniform&gt;
 *     &lt;object&gt;
 *       &lt;tileset&gt;
 *         &lt;!-- ... --&gt;
 *       &lt;/tileset&gt;
 *     &lt;/object&gt;
 *   &lt;/tilesets&gt;
 * </pre>
 *
 * The class specified in the <code>ruleset</code> element must derive
 * from {@link TileSetRuleSet}. The images that will be included in the
 * bundle must be in the same directory as the bundle description file and
 * the tileset descriptions must reference the images without a preceding
 * path.
 */
public class TileSetBundler
{
    /**
     * Constructs a tileset bundler with the specified path to a bundler
     * configuration file. The configuration file will be loaded and used
     * to configure this tileset bundler.
     */
    public TileSetBundler (String configPath)
        throws IOException
    {
        this(new File(configPath));
    }

    /**
     * Constructs a tileset bundler with the specified bundler config
     * file.
     */
    public TileSetBundler (File configFile)
        throws IOException
    {
        // we parse our configuration with a digester
        Digester digester = new Digester();

        // push our mappings array onto the stack
        ArrayList mappings = new ArrayList();
        digester.push(mappings);

        // create a mapping object for each mapping entry and append it to
        // our mapping list
        digester.addObjectCreate("bundler-config/mapping",
                                 Mapping.class.getName());
        digester.addSetNext("bundler-config/mapping",
                            "add", "java.lang.Object");

        // configure each mapping object with the path and ruleset
        digester.addCallMethod("bundler-config/mapping", "init", 2);
        digester.addCallParam("bundler-config/mapping/path", 0);
        digester.addCallParam("bundler-config/mapping/ruleset", 1);

        // now go like the wind
        FileInputStream fin = new FileInputStream(configFile);
        try {
            digester.parse(fin);
        } catch (SAXException saxe) {
            String errmsg = "Failure parsing bundler config file " +
                "[file=" + configFile.getPath() + "]";
            throw new NestableIOException(errmsg, saxe);
        }
        fin.close();

        // create our digester
        _digester = new Digester();

        // use the mappings we parsed to configure our actual digester
        int msize = mappings.size();
        for (int i = 0; i < msize; i++) {
            Mapping map = (Mapping)mappings.get(i);
            try {
                TileSetRuleSet ruleset = (TileSetRuleSet)
                    Class.forName(map.ruleset).newInstance();

                // configure the ruleset
                ruleset.setPrefix(map.path);
                // add it to the digester
                _digester.addRuleSet(ruleset);
                // and add a rule to stick the parsed tilesets onto the
                // end of an array list that we'll put on the stack
                _digester.addSetNext(map.path + TileSetRuleSet.TILESET_PATH,
                                     "add", "java.lang.Object");

            } catch (Exception e) {
                String errmsg = "Unable to create tileset rule set " +
                    "instance [mapping=" + map + "].";
                throw new NestableIOException(errmsg, e);
            }
        }
    }

    /**
     * Creates a tileset bundle at the location specified by the
     * <code>targetPath</code> parameter, based on the description
     * provided via the <code>bundleDesc</code> parameter.
     *
     * @param idBroker the tileset id broker that will be used to map
     * tileset names to tileset ids.
     * @param bundleDef a file object pointing to the bundle description
     * file.
     * @param targetPath the path of the tileset bundle file that will be
     * created.
     *
     * @exception IOException thrown if an error occurs reading, writing
     * or processing anything.
     */
    public void createBundle (
        TileSetIDBroker idBroker, File bundleDesc, String targetPath)
        throws IOException
    {
        createBundle(idBroker, bundleDesc, new File(targetPath));
    }

    /**
     * Creates a tileset bundle at the location specified by the
     * <code>targetPath</code> parameter, based on the description
     * provided via the <code>bundleDesc</code> parameter.
     *
     * @param idBroker the tileset id broker that will be used to map
     * tileset names to tileset ids.
     * @param bundleDesc a file object pointing to the bundle description
     * file.
     * @param target the tileset bundle file that will be created.
     *
     * @return true if the bundle was rebuilt, false if it was not because
     * the bundle file was newer than all involved source files.
     *
     * @exception IOException thrown if an error occurs reading, writing
     * or processing anything.
     */
    public boolean createBundle (
        TileSetIDBroker idBroker, final File bundleDesc, File target)
        throws IOException
    {
        // stick an array list on the top of the stack into which we will
        // collect parsed tilesets
        ArrayList sets = new ArrayList();
        _digester.push(sets);

        // parse the tilesets
        FileInputStream fin = new FileInputStream(bundleDesc);
        try {
            _digester.parse(fin);
        } catch (SAXException saxe) {
            String errmsg = "Failure parsing bundle description file " +
                "[path=" + bundleDesc.getPath() + "]";
            throw new NestableIOException(errmsg, saxe);
        } finally {
            fin.close();
        }

        // we want to make sure that at least one of the tileset image
        // files or the bundle definition file is newer than the bundle
        // file, otherwise consider the bundle up to date
        long newest = bundleDesc.lastModified();

        // create a tileset bundle to hold our tilesets
        TileSetBundle bundle = new TileSetBundle();

        // add all of the parsed tilesets to the tileset bundle
        try {
            for (int i = 0; i < sets.size(); i++) {
                TileSet set = (TileSet)sets.get(i);
                String name = set.getName();

                // let's be robust
                if (name == null) {
                    Log.warning("Tileset was parsed, but received no name " +
                                "[set=" + set + "]. Skipping.");
                    continue;
                }

                // make sure this tileset's image file exists and check
                // it's last modified date
                File tsfile = new File(bundleDesc.getParent(),
                                       set.getImagePath());
                if (!tsfile.exists()) {
                    System.err.println("Tile set missing image file " +
                                       "[bundle=" + bundleDesc.getPath() +
                                       ", name=" + set.getName() +
                                       ", imgpath=" + tsfile.getPath() + "].");
                    continue;
                }
                if (tsfile.lastModified() > newest) {
                    newest = tsfile.lastModified();
                }

                // assign a tilset id to the tileset and bundle it
                try {
                    int tileSetId = idBroker.getTileSetID(name);
                    bundle.addTileSet(tileSetId, set);
                } catch (PersistenceException pe) {
                    String errmsg = "Failure obtaining a tileset id for " +
                        "tileset [set=" + set + "].";
                    throw new NestableIOException(errmsg, pe);
                }
            }

            // clear out our array list in preparation for another go
            sets.clear();

        } finally {
            // before we go, we have to commit our brokered tileset ids
            // back to the broker's persistent store
            try {
                idBroker.commit();
            } catch (PersistenceException pe) {
                Log.warning("Failure committing brokered tileset ids " +
                            "back to broker's persistent store " +
                            "[error=" + pe + "].");
            }
        }

        // see if our newest file is newer than the tileset bundle
        if (newest < target.lastModified()) {
            return false;
        }

        // now we have to create the actual bundle file
        FileOutputStream fout = new FileOutputStream(target);
        Manifest manifest = new Manifest();
        JarOutputStream jar = new JarOutputStream(fout, manifest);
        jar.setLevel(Deflater.BEST_COMPRESSION);

        // create an image provider for loading our tileset images
        SimpleCachingImageProvider improv = new SimpleCachingImageProvider() {
            protected BufferedImage loadImage (String path)
                throws IOException {
                return ImageIO.read(new File(bundleDesc.getParent(), path));
            }
        };

        try {
            // write all of the image files to the bundle, converting the
            // tilesets to trimmed tilesets in the process
            Iterator iditer = bundle.enumerateTileSetIds();
            while (iditer.hasNext()) {
                int tileSetId = ((Integer)iditer.next()).intValue();
                TileSet set = bundle.getTileSet(tileSetId);
                String imagePath = set.getImagePath();

                // sanity checks
                if (imagePath == null) {
                    Log.warning("Tileset contains no image path " +
                                "[set=" + set + "]. It ain't gonna work.");
                    continue;
                }

                // if this is an object tileset, we can't trim it!
                if (set instanceof ObjectTileSet) {
                    // set the tileset up with an image provider; we
                    // need to do this so that we can trim it!
                    set.setImageProvider(improv);

                    // we're going to trim it, so adjust the path
                    imagePath = adjustImagePath(imagePath);
                    jar.putNextEntry(new JarEntry(imagePath));

                    try {
                        // create a trimmed object tileset, which will
                        // write the trimmed tileset image to the jar
                        // output stream
                        TrimmedObjectTileSet tset =
                            TrimmedObjectTileSet.trimObjectTileSet(
                                (ObjectTileSet)set, jar);
                        tset.setImagePath(imagePath);
                        // replace the original set with the trimmed
                        // tileset in the tileset bundle
                        bundle.addTileSet(tileSetId, tset);

                    } catch (Exception e) {
                        System.err.println("Error adding tileset to bundle " +
                                           "[set=" + set.getName() +
                                           ", ipath=" + imagePath + "].");
                        e.printStackTrace(System.err);
                        // replace the tileset with an error tileset
                        UniformTileSet ets = new UniformTileSet();
                        ets.setName(set.getName());
                        ets.setWidth(50);
                        ets.setHeight(50);
                        ets.setImagePath(imagePath);
                        bundle.addTileSet(tileSetId, ets);
                        // and write an error image to the jar file
                        ImageIO.write(ImageUtil.createErrorImage(50, 50),
                                      "PNG", jar);
                    }

                } else {
                    // read the image file and convert it to our custom
                    // format in the bundle
                    File ifile = new File(bundleDesc.getParent(), imagePath);
                    try {
                        BufferedImage image = ImageIO.read(ifile);
                        if (FastImageIO.canWrite(image)) {
                            imagePath = adjustImagePath(imagePath);
                            jar.putNextEntry(new JarEntry(imagePath));
                            set.setImagePath(imagePath);
                            FastImageIO.write(image, jar);
                        } else {
                            jar.putNextEntry(new JarEntry(imagePath));
                            FileInputStream imgin = new FileInputStream(ifile);
                            StreamUtils.pipe(imgin, jar);
                        }
                    } catch (Exception e) {
                        throw new NestableIOException(
                            "Failure bundling image " + ifile + ": " + e, e);
                    }
                }
            }

            // now write a serialized representation of the tileset bundle
            // object to the bundle jar file
            JarEntry entry = new JarEntry(BundleUtil.METADATA_PATH);
            jar.putNextEntry(entry);
            ObjectOutputStream oout = new ObjectOutputStream(jar);
            oout.writeObject(bundle);
            oout.flush();

            // finally close up the jar file and call ourself done
            jar.close();

            return true;

        } catch (Exception e) {
            // remove the incomplete jar file and rethrow the exception
            jar.close();
            if (!target.delete()) {
                Log.warning("Failed to close botched bundle '" + target + "'.");
            }
            String errmsg = "Failed to create bundle " + target + ": " + e;
            throw new NestableIOException(errmsg, e);
        }
    }

    /** Replaces the image suffix with <code>.raw</code>. */
    protected String adjustImagePath (String imagePath)
    {
        int didx = imagePath.lastIndexOf(".");
        return ((didx == -1) ? imagePath :
                imagePath.substring(0, didx)) + ".raw";
    }

    /** Used to parse our configuration. */
    public static class Mapping
    {
        public String path;
        public String ruleset;

        public void init (String path, String ruleset)
        {
            this.path = path;
            this.ruleset = ruleset;
        }

        public String toString ()
        {
            return "[path=" + path + ", ruleset=" + ruleset + "]";
        }
    }

    /** The digester we use to parse bundle descriptions. */
    protected Digester _digester;
}
