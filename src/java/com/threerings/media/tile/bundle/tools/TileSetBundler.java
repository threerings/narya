//
// $Id: TileSetBundler.java,v 1.3 2001/11/29 21:58:15 mdb Exp $

package com.threerings.media.tools.tile.bundle;

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

import org.xml.sax.SAXException;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;

import org.apache.commons.util.StreamUtils;

import com.samskivert.io.NestableIOException;
import com.samskivert.io.PersistenceException;

import com.threerings.media.Log;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileSetIDBroker;
import com.threerings.media.tile.bundle.BundleUtil;
import com.threerings.media.tile.bundle.TileSetBundle;
import com.threerings.media.tools.tile.xml.TileSetRuleSet;

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
 * <bundler-config>
 *   <mapping>
 *     <path>bundle.tilesets.uniform</path>
 *     <ruleset>
 *       com.threerings.media.tools.tile.xml.UniformTileSetRuleSet
 *     </ruleset>
 *   </mapping>
 *   <mapping>
 *     <path>bundle.tilesets.object</path>
 *     <ruleset>
 *       com.threerings.media.tools.tile.xml.ObjectTileSetRuleSet
 *     </ruleset>
 *   </mapping>
 * </bundler-config>
 * </pre>
 *
 * This configuration would be used to parse a bundle description that
 * looked something like the following:
 *
 * <pre>
 * <bundle>
 *   <tilesets>
 *     <uniform>
 *       <tileset>
 *         <!-- ... -->
 *       </tileset>
 *     </uniform>
 *     <object>
 *       <tileset>
 *         <!-- ... -->
 *       </tileset>
 *     </object>
 *   </tilesets>
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
            String errmsg = "Failure parsing bundler config file.";
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
     * @param bundleDef a file object pointing to the bundle description
     * file.
     * @param target the tileset bundle file that will be created.
     *
     * @exception IOException thrown if an error occurs reading, writing
     * or processing anything.
     */
    public void createBundle (
        TileSetIDBroker idBroker, File bundleDesc, File target)
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
            String errmsg = "Failure parsing bundle description file.";
            throw new NestableIOException(errmsg, saxe);
        } finally {
            fin.close();
        }

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

        // now we have to create the actual bundle file
        FileOutputStream fout = new FileOutputStream(target);
        Manifest manifest = new Manifest();
        JarOutputStream jar = new JarOutputStream(fout, manifest);

        try {
            // first write a serialized representation of the tileset
            // bundle object to the bundle jar file
            JarEntry entry = new JarEntry(BundleUtil.METADATA_PATH);
            jar.putNextEntry(entry);
            ObjectOutputStream oout = new ObjectOutputStream(jar);
            oout.writeObject(bundle);
            oout.flush();

            // now write all of the image files to the bundle
            Iterator setiter = bundle.enumerateTileSets();
            while (setiter.hasNext()) {
                TileSet set = (TileSet)setiter.next();
                String imagePath = set.getImagePath();

                // sanity checks
                if (imagePath == null) {
                    Log.warning("Tileset contains no image path " +
                                "[set=" + set + "]. It ain't gonna work.");
                    continue;
                }

                // open the image and pipe it into the jar file
                File imgfile = new File(bundleDesc.getParent(), imagePath);
                FileInputStream imgin = new FileInputStream(imgfile);
                jar.putNextEntry(new JarEntry(imagePath));
                StreamUtils.pipe(imgin, jar);
            }

            // finally close up the jar file and call ourself done
            jar.close();

        } catch (IOException ioe) {
            // remove the incomplete jar file and rethrow the exception
            fout.close();
            target.delete();
            throw ioe;
        }
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
