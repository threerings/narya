//
// $Id: ComponentBundlerTask.java,v 1.3 2002/02/05 20:29:09 mdb Exp $

package com.threerings.cast.bundle.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;

import org.apache.commons.util.StreamUtils;

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
        // first strip off the file extension
        if (!path.endsWith(BundleUtil.IMAGE_EXTENSION)) {
            throw new BuildException("Can't bundle malformed image file " +
                                     "[path=" + path + "].");
        }
        path = path.substring(0, path.length() -
                              BundleUtil.IMAGE_EXTENSION.length());

        // now decompose the path
        String[] components = StringUtil.split(path, File.separator);
        int clen = components.length;
        if (clen < 3) {
            throw new BuildException("Can't bundle malformed image file " +
                                     "[path=" + path + "].");
        }
        String[] info = new String[3];
        System.arraycopy(components, clen-3, info, 0, 3);
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
        HashMapIDBroker broker;

        try {
            FileInputStream fin = new FileInputStream(mapfile);
            ObjectInputStream oin = new ObjectInputStream(fin);
            broker = (HashMapIDBroker)oin.readObject();

        } catch (FileNotFoundException fnfe) {
            // if the file doesn't yet exist, start with a blank broker
            broker = new HashMapIDBroker();

        } catch (Exception e) {
            throw new BuildException("Error loading component ID map.", e);
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
        try {
            FileOutputStream fout = new FileOutputStream(mapfile);
            ObjectOutputStream oout = new ObjectOutputStream(fout);
            oout.writeObject(broker);
            oout.close();
        } catch (IOException ioe) {
            throw new BuildException("Unable to store component ID map.", ioe);
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

        protected int _nextCID = 0;
    }

    /** The path to our component bundle file. */
    protected File _target;

    /** The path to our component map file. */
    protected File _mapfile;

    /** A list of filesets that contain tile images. */
    protected ArrayList _filesets = new ArrayList();
}
