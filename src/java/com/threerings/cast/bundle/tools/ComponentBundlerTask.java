//
// $Id: ComponentBundlerTask.java,v 1.8 2002/04/11 01:33:51 mdb Exp $

package com.threerings.cast.bundle.tools;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;

import org.apache.commons.io.StreamUtils;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.threerings.cast.ComponentIDBroker;
import com.threerings.cast.bundle.BundleUtil;

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

        // load up our component ID broker
        ComponentIDBroker broker = loadBroker(_mapfile);

        try {
            // make sure we can create our bundle file
            FileOutputStream fout = new FileOutputStream(_target);
            JarOutputStream jout = new JarOutputStream(fout);

            // we'll fill this with component id to tuple mappings as we go
            HashIntMap mapping = new HashIntMap();

            // deal with the filesets
            for (int i = 0; i < _filesets.size(); i++) {
                FileSet fs = (FileSet)_filesets.get(i);
                DirectoryScanner ds = fs.getDirectoryScanner(project);
                File fromDir = fs.getDir(project);
                String[] srcFiles = ds.getIncludedFiles();

                for (int f = 0; f < srcFiles.length; f++) {
                    File cfile = new File(fromDir, srcFiles[f]);
                    // determine the [class, name, action] triplet
                    String[] info = decomposePath(cfile.getPath());
                    // obtain the component id from our id broker
                    int cid = broker.getComponentID(info[0], info[1]);
                    // add a mapping for this component
                    mapping.put(cid, new Tuple(info[0], info[1]));
                    // construct the path that'll go in the jar file and
                    // stuff the component into the jarfile
                    jout.putNextEntry(new JarEntry(composePath(info)));
                    StreamUtils.pipe(new FileInputStream(cfile), jout);
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
    protected String composePath (String[] info)
    {
        return (info[0] + File.separator + info[1] + File.separator +
                info[2] + BundleUtil.IMAGE_EXTENSION);
    }

    protected void ensureSet (Object value, String errmsg)
        throws BuildException
    {
        if (value == null) {
            throw new BuildException(errmsg);
        }
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
            FileInputStream fin = new FileInputStream(mapfile);
            DataInputStream din =
                new DataInputStream(new BufferedInputStream(fin));
            broker.readFrom(din);

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
            FileOutputStream fout = new FileOutputStream(mapfile);
            DataOutputStream dout =
                new DataOutputStream(new BufferedOutputStream(fout));
            hbroker.writeTo(dout);
            dout.close();
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
                cid = new Integer(++_nextCID);
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

        public void writeTo (DataOutputStream dout)
            throws IOException
        {
            // write out the size of the table
            dout.writeInt(size());
            // write out the keys and values
            Iterator keys = keySet().iterator();
            while (keys.hasNext()) {
                Tuple key = (Tuple)keys.next();
                Integer value = (Integer)get(key);
                dout.writeUTF((String)key.left);
                dout.writeUTF((String)key.right);
                dout.writeInt(value.intValue());
            }
            // write out our most recently assigned component id
            dout.writeInt(_nextCID);
        }

        public void readFrom (DataInputStream din)
            throws IOException
        {
            // figure out how many keys and values we'll be reading
            int size = din.readInt();
            // and read them on in
            for (int i = 0; i < size; i++) {
                String cclass = din.readUTF();
                String cname = din.readUTF();
                int value = din.readInt();
                put(new Tuple(cclass, cname), new Integer(value));
            }
            // read in our most recently assigned component id
            _nextCID = din.readInt();
            // keep track of this so that we can tell if we were modified
            _startCID = _nextCID;
        }

        protected int _nextCID = 0;

        protected int _startCID = 0;
    }

    /** The path to our component bundle file. */
    protected File _target;

    /** The path to our component map file. */
    protected File _mapfile;

    /** The component directory root. */
    protected String _root;

    /** A list of filesets that contain tile images. */
    protected ArrayList _filesets = new ArrayList();
}
