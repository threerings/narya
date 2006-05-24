//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.cast.bundle.tools;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.zip.Deflater;

import org.apache.commons.digester.Digester;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.ComparableArrayList;
import com.samskivert.util.FileUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Tuple;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.threerings.media.tile.ImageProvider;
import com.threerings.media.tile.SimpleCachingImageProvider;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TrimmedTileSet;
import com.threerings.media.tile.tools.xml.SwissArmyTileSetRuleSet;

import com.threerings.cast.ComponentIDBroker;
import com.threerings.cast.StandardActions;
import com.threerings.cast.bundle.BundleUtil;
import com.threerings.cast.tools.xml.ActionRuleSet;

/**
 * Ant task for creating component bundles. This task must be configured
 * with a number of parameters:
 *
 * <pre>
 * target=[path to bundle file, which will be created]
 * mapfile=[path to the component map file which maintains a mapping from
 *          component id to component class/name, it will be created the
 *          first time and updated as new components are mapped]
 * </pre>
 *
 * It should also contain one or more nested &lt;fileset&gt; elements that
 * enumerate the action tileset images that should be included in the
 * component bundle.
 */
public class ComponentBundlerTask extends Task
{
    /**
     * Sets the path to the bundle file that we'll be creating.
     */
    public void setTarget (File target)
    {
        _target = target;
    }

    /**
     * Sets the path to the component map file that we'll use to obtain
     * component ids for the bundled components.
     */
    public void setMapfile (File mapfile)
    {
        _mapfile = mapfile;
    }

    /**
     * Sets the path to the action tilesets definition file.
     */
    public void setActiondef (File actiondef)
    {
        _actionDef = actiondef;
    }

    /**
     * Sets the root path which will be stripped from the image paths
     * prior to parsing them to obtain the component class, type and
     * action names.
     */
    public void setRoot (File root)
    {
        _root = root.getPath();
    }

    /**
     * Adds a nested &lt;fileset&gt; element.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        // make sure everything was set up properly
        ensureSet(_target, "Must specify the path to the target bundle " +
                  "file via the 'target' attribute.");
        ensureSet(_mapfile, "Must specify the path to the component map " +
                  "file via the 'mapfile' attribute.");
        ensureSet(_actionDef, "Must specify the action sequence " +
                  "definitions via the 'actiondef' attribute.");

        // parse in the action tilesets
        HashMap actsets = parseActionTileSets();

        // load up our component ID broker
        ComponentIDBroker broker = loadBroker(_mapfile);

        // check to see if any of the source files are newer than the
        // target file
        ArrayList sources = (ArrayList)_filesets.clone();
        sources.add(_mapfile);
        sources.add(_actionDef);
        if (!outOfDate(sources, _target)) {
            System.out.println(_target.getPath() + " is up to date.");
            return;
        }

        // create an image provider for loading our component images
        ImageProvider improv = new SimpleCachingImageProvider() {
            protected BufferedImage loadImage (String path)
                throws IOException {
                return ImageIO.read(new File(path));
            }
        };

        System.out.println("Generating " + _target.getPath() + "...");

        try {
            // make sure we can create our bundle file
            FileOutputStream fout = new FileOutputStream(_target);
            JarOutputStream jout = new JarOutputStream(fout);
            jout.setLevel(Deflater.BEST_COMPRESSION);

            // we'll fill this with component id to tuple mappings
            HashIntMap mapping = new HashIntMap();

            // herein we'll insert trimmed tileset objects that go along
            // with each of the trimmed action images
            HashIntMap actionSets = new HashIntMap();

            // deal with the filesets
            for (int i = 0; i < _filesets.size(); i++) {
                FileSet fs = (FileSet)_filesets.get(i);
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                File fromDir = fs.getDir(getProject());
                String[] srcFiles = ds.getIncludedFiles();

                for (int f = 0; f < srcFiles.length; f++) {
                    File cfile = new File(fromDir, srcFiles[f]);
                    // determine the [class, name, action] triplet
                    String[] info = decomposePath(cfile.getPath());

                    // make sure we have an action tileset definition
                    TileSet aset = (TileSet)actsets.get(info[2]);
                    if (aset == null) {
                        System.err.println(
                            "No tileset definition for component action " +
                            "[class=" + info[0] + ", name=" + info[1] +
                            ", action=" + info[2] + "].");
                        continue;
                    }
                    aset.setImageProvider(improv);

                    // obtain the component id from our id broker
                    int cid = broker.getComponentID(info[0], info[1]);
                    // add a mapping for this component
                    mapping.put(cid, new Tuple(info[0], info[1]));

                    // process and store the main component image
                    processComponent(info, aset, cfile, jout);

                    // pick up any auxiliary images as well like the shadow or
                    // crop files
                    String action = info[2];
                    String ext = BundleUtil.IMAGE_EXTENSION;
                    for (int aa = 0; aa < AUX_EXTS.length; aa++) {
                        File afile = new File(
                            FileUtil.resuffix(cfile, ext, AUX_EXTS[aa] + ext));
                        if (afile.exists()) {
                            info[2] = action + AUX_EXTS[aa];
                            processComponent(info, aset, afile, jout);
                        }
                    }
                }
            }

            // write our mapping table to the jar file as well
            jout.putNextEntry(new JarEntry(BundleUtil.COMPONENTS_PATH));
            ObjectOutputStream oout = new ObjectOutputStream(jout);
            oout.writeObject(mapping);
            oout.flush();

            // seal up our jar file
            jout.close();

        } catch (IOException ioe) {
            String errmsg = "Unable to create component bundle.";
            throw new BuildException(errmsg, ioe);

        } catch (PersistenceException pe) {
            String errmsg = "Unable to obtain component ID mapping.";
            throw new BuildException(errmsg, pe);
        }

        // save our updated component ID broker
        saveBroker(_mapfile, broker);
    }

    protected void processComponent (
        String[] info, TileSet aset, File cfile, JarOutputStream jout)
        throws IOException
    {
        // construct the path that'll go in the jar file
        String ipath = composePath(
            info, BundleUtil.IMAGE_EXTENSION);
        jout.putNextEntry(new JarEntry(ipath));

        // create a trimmed tileset based on the source action tileset and
        // stuff the new trimmed image into the jar file at the same time
        aset.setImagePath(cfile.getPath());

        TrimmedTileSet tset = null;
        try {
            tset = TrimmedTileSet.trimTileSet(aset, jout);
            tset.setImagePath(ipath);
        } catch (Throwable t) {
            System.err.println(
                "Failure trimming tileset " +
                "[class=" + info[0] + ", name=" + info[1] +
                ", action=" + info[2] +
                ", srcimg=" + aset.getImagePath() + "].");
            t.printStackTrace(System.err);
        }

        // then write our trimmed tileset to the jar file
        if (tset != null) {
            String tpath = composePath(
                info, BundleUtil.TILESET_EXTENSION);
            jout.putNextEntry(new JarEntry(tpath));
            ObjectOutputStream oout = new ObjectOutputStream(jout);
            oout.writeObject(tset);
            oout.flush();
        }
    }

    protected boolean outOfDate (ArrayList sources, File target)
    {
        for (int ii = 0; ii < sources.size(); ii++) {
            if (outOfDate(sources.get(ii), target)) {
                return true;
            }
        }
        return false;
    }

    protected boolean outOfDate (Object source, File target)
    {
        if (source instanceof FileSet) {
            FileSet fs = (FileSet)source;
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (int f = 0; f < srcFiles.length; f++) {
                File cfile = new File(fromDir, srcFiles[f]);
                if (newer(cfile, target)) {
                    return true;
                }
            }
            return false;

        } else if (source instanceof File) {
            return newer((File)source, target);

        } else {
            System.err.println("Can't compare " + source +
                               " to " + target + ".");
            return true;
        }
    }

    /**
     * Returns true if <code>source</code> is newer than
     * <code>target</code>.
     */
    protected boolean newer (File source, File target)
    {
        return source.lastModified() > target.lastModified();
    }

    /**
     * Decomposes the full path to a component image into a [class, name,
     * action] triplet.
     */
    protected String[] decomposePath (String path)
        throws BuildException
    {
        // first strip off the root
        if (!path.startsWith(_root)) {
            throw new BuildException("Can't bundle images outside the " +
                                     "root directory [root=" + _root +
                                     ", path=" + path + "].");
        }
        path = path.substring(_root.length());

        // strip off any preceding slash
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        // now strip off the file extension
        if (!path.endsWith(BundleUtil.IMAGE_EXTENSION)) {
            throw new BuildException("Can't bundle malformed image file " +
                                     "[path=" + path + "].");
        }
        path = path.substring(0, path.length() -
                              BundleUtil.IMAGE_EXTENSION.length());

        // now decompose the path; the component type and action must
        // always be a single string but the class can span multiple
        // directories for easier component organization; thus
        // "male/head/goatee/standing" will be parsed as [class=male/head,
        // type=goatee, action=standing]
        String malmsg = "Can't decode malformed image path: '" + path + "'";
        String[] info = new String[3];
        int lsidx = path.lastIndexOf(File.separator);
        if (lsidx == -1) {
            throw new BuildException(malmsg);
        }
        info[2] = path.substring(lsidx+1);
        int slsidx = path.lastIndexOf(File.separator, lsidx-1);
        if (slsidx == -1) {
            throw new BuildException(malmsg);
        }
        info[1] = path.substring(slsidx+1, lsidx);
        info[0] = path.substring(0, slsidx);
        return info;
    }

    /**
     * Composes a triplet of [class, name, action] into the path that
     * should be supplied to the JarEntry that contains the associated
     * image data.
     */
    protected String composePath (String[] info, String extension)
    {
        return (info[0] + File.separator + info[1] +
                File.separator + info[2] + extension);
    }

    protected void ensureSet (Object value, String errmsg)
        throws BuildException
    {
        if (value == null) {
            throw new BuildException(errmsg);
        }
    }

    /**
     * Parses the action tileset definitions and puts them into a hash
     * map, keyed on action name.
     */
    protected HashMap parseActionTileSets ()
    {
        Digester digester = new Digester();
        SwissArmyTileSetRuleSet srules = new SwissArmyTileSetRuleSet();
        String aprefix = "actions" + ActionRuleSet.ACTION_PATH;
        srules.setPrefix(aprefix);

        digester.addRuleSet(srules);
        digester.addSetProperties(aprefix);
        digester.addSetNext(aprefix + SwissArmyTileSetRuleSet.TILESET_PATH,
                            "addTileSet", TileSet.class.getName());

        HashMap actsets = new ActionMap();
        digester.push(actsets);

        try {
            FileInputStream fin = new FileInputStream(_actionDef);
            BufferedInputStream bin = new BufferedInputStream(fin);
            digester.parse(bin);

        } catch (FileNotFoundException fnfe) {
            String errmsg = "Unable to load action definition file " +
                "[path=" + _actionDef.getPath() + "].";
            throw new BuildException(errmsg, fnfe);

        } catch (Exception e) {
            throw new BuildException("Parsing error.", e);
        }

        return actsets;
    }

    /** Used when parsing action tilesets. */
    public static class ActionMap extends HashMap
    {
        public void setName (String name) {
            _name = name;
        }
        public void addTileSet (TileSet set) {
            set.setName(_name);
            put(_name, set);
        }
        protected String _name;
    }

    /**
     * Loads the hashmap ID broker from its persistent representation in
     * the specified file.
     */
    protected HashMapIDBroker loadBroker (File mapfile)
        throws BuildException
    {
        HashMapIDBroker broker = new HashMapIDBroker();

        try {
            BufferedReader bin = new BufferedReader(new FileReader(mapfile));
            broker.readFrom(bin);
            bin.close();

        } catch (FileNotFoundException fnfe) {
            // if the file doesn't yet exist, start with a blank broker

        } catch (Exception e) {
            throw new BuildException("Error loading component ID map " +
                                     "[mapfile=" + mapfile + "]", e);
        }

        return broker;
    }

    /**
     * Stores a persistent representation of the supplied hashmap ID
     * broker in the specified file.
     */
    protected void saveBroker (File mapfile, ComponentIDBroker broker)
        throws BuildException
    {
        HashMapIDBroker hbroker = (HashMapIDBroker)broker;

        // bail if the broker wasn't modified
        if (!hbroker.isModified()) {
            return;
        }

        try {
            BufferedWriter bout = new BufferedWriter(new FileWriter(mapfile));
            hbroker.writeTo(bout);
            bout.close();
        } catch (IOException ioe) {
            throw new BuildException("Unable to store component ID map " +
                                     "[mapfile=" + mapfile + "]", ioe);
        }
    }

    protected static class HashMapIDBroker
        extends HashMap implements ComponentIDBroker
    {
        public int getComponentID (String cclass, String cname)
            throws PersistenceException
        {
            Tuple key = new Tuple(cclass, cname);
            Integer cid = (Integer)get(key);
            if (cid == null) {
                cid = Integer.valueOf(++_nextCID);
                put(key, cid);
            }
            return cid.intValue();
        }

        public void commit ()
            throws PersistenceException
        {
            // nothing doing
        }

        public boolean isModified ()
        {
            return _nextCID != _startCID;
        }

        public void writeTo (BufferedWriter bout)
            throws IOException
        {
            // write out our most recently assigned component id
            String cidline = "" + _nextCID;
            bout.write(cidline, 0, cidline.length());
            bout.newLine();

            // write out the keys and values
            ComparableArrayList lines = new ComparableArrayList();
            Iterator keys = keySet().iterator();
            while (keys.hasNext()) {
                Tuple key = (Tuple)keys.next();
                Integer value = (Integer)get(key);
                String line = key.left + SEP_STR + key.right + SEP_STR + value;
                lines.add(line);
            }

            // sort the output
            lines.sort();

            // now write it to the file
            int lcount = lines.size();
            for (int ii = 0; ii < lcount; ii++) {
                String line = (String)lines.get(ii);
                bout.write(line, 0, line.length());
                bout.newLine();
            }
        }

        public void readFrom (BufferedReader bin)
            throws IOException
        {
            // read in our most recently assigned component id
            _nextCID = readInt(bin);
            // keep track of this so that we can tell if we were modified
            _startCID = _nextCID;

            // now read in our keys and values
            String line;
            while ((line = bin.readLine()) != null) {
                String orig = line;
                int sidx = line.indexOf(SEP_STR);
                if (sidx == -1) {
                    throw new IOException("Malformed line '" + orig + "'");
                }
                String cclass = line.substring(0, sidx);
                line = line.substring(sidx + SEP_STR.length());
                sidx = line.indexOf(SEP_STR);
                if (sidx == -1) {
                    throw new IOException("Malformed line '" + orig + "'");
                }
                String cname = line.substring(0, sidx);
                line = line.substring(sidx + SEP_STR.length());
                try {
                    put(new Tuple(cclass, cname), Integer.valueOf(line));
                } catch (NumberFormatException nfe) {
                    String err = "Malformed line, invalid code '" + orig + "'";
                    throw new IOException(err);
                }
            }
        }

        protected int readInt (BufferedReader bin)
            throws IOException
        {
            String line = bin.readLine();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException nfe) {
                throw new IOException("Expected number, got '" + line + "'");
            }
        }

        protected int _nextCID = 0;
        protected int _startCID = 0;
    }

    /** The path to our component bundle file. */
    protected File _target;

    /** The path to our component map file. */
    protected File _mapfile;

    /** The path to our action tilesets definition file. */
    protected File _actionDef;

    /** The component directory root. */
    protected String _root;

    /** A list of filesets that contain tile images. */
    protected ArrayList _filesets = new ArrayList();

    /** Used to separate keys and values in the map file. */
    protected static final String SEP_STR = " := ";

    /** Used to process auxilliary tilesets. */
    protected static final String[] AUX_EXTS = {
        "_" + StandardActions.SHADOW_TYPE,
        "_" + StandardActions.CROP_TYPE };
}
