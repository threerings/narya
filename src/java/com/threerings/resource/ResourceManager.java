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

package com.threerings.resource;

import java.awt.EventQueue;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import java.security.AccessController;
import java.security.PrivilegedAction;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.samskivert.net.PathUtil;
import com.samskivert.util.ResultListener;
import com.samskivert.util.StringUtil;

/**
 * The resource manager is responsible for maintaining a repository of
 * resources that are synchronized with a remote source. This is
 * accomplished in the form of sets of jar files (resource bundles) that
 * contain resources and that are updated from a remote resource
 * repository via HTTP.  These resource bundles are organized into
 * resource sets. A resource set contains one or more resource bundles and
 * is defined much like a classpath.
 *
 * <p> The resource manager can load resources from the default resource
 * set, and can make available named resource sets to entities that wish
 * to do their own resource loading. If the resource manager fails to
 * locate a resource in the default resource set, it falls back to loading
 * the resource via the classloader (which will search the classpath).
 *
 * <p> Applications that wish to make use of resource sets and their
 * associated bundles must call {@link #initBundles} after constructing
 * the resource manager, providing the path to a resource definition file
 * which describes these resource sets. The definition file will be loaded
 * and the resource bundles defined within will be loaded relative to the
 * resource directory.  The bundles will be cached in the user's home
 * directory and only reloaded when the source resources have been
 * updated. The resource definition file looks something like the
 * following:
 *
 * <pre>
 * resource.set.default = sets/misc/config.jar: \
 *                        sets/misc/icons.jar
 * resource.set.tiles = sets/tiles/ground.jar: \
 *                      sets/tiles/objects.jar: \
 *                      /global/resources/tiles/ground.jar: \
 *                      /global/resources/tiles/objects.jar
 * resource.set.sounds = sets/sounds/sfx.jar: \
 *                       sets/sounds/music.jar: \
 *                       /global/resources/sounds/sfx.jar: \
 *                       /global/resources/sounds/music.jar
 * </pre>
 *
 * <p> All resource set definitions are prefixed with
 * <code>resource.set.</code> and all text following that string is
 * considered to be the name of the resource set. The resource set named
 * <code>default</code> is the default resource set and is the one that is
 * searched for resources is a call to {@link #getResource}.
 *
 * <p> When a resource is loaded from a resource set, the set is searched
 * in the order that entries are specified in the definition.
 */
public class ResourceManager
{
    /**
     * Provides facilities for notifying an observer of the resource
     * unpacking process.
     */
    public interface InitObserver
    {
        /**
         * Indicates a percent completion along with an estimated time
         * remaining in seconds.
         */
        public void progress (int percent, long remaining);

        /**
         * Indicates that there was a failure unpacking our resource
         * bundles.
         */
        public void initializationFailed (Exception e);
    }

    /**
     * An adapter that wraps an {@link InitObserver} and routes all method
     * invocations to the AWT thread.
     */
    public static class AWTInitObserver implements InitObserver
    {
        public AWTInitObserver (InitObserver obs) {
            _obs = obs;
        }

        public void progress (final int percent, final long remaining) {
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    _obs.progress(percent, remaining);
                }
            });
        }

        public void initializationFailed (final Exception e) {
            EventQueue.invokeLater(new Runnable() {
                public void run () {
                    _obs.initializationFailed(e);
                }
            });
        }

        protected InitObserver _obs;
    }

    /**
     * Constructs a resource manager which will load resources via the
     * classloader, prepending <code>resourceRoot</code> to their path.
     *
     * @param resourceRoot the path to prepend to resource paths prior to
     * attempting to load them via the classloader. When resources are
     * bundled into the default resource bundle, they don't need this
     * prefix, but if they're to be loaded from the classpath, it's likely
     * that they'll live in some sort of <code>resources</code> directory
     * to isolate them from the rest of the files in the classpath. This
     * is not a platform dependent path (forward slash is always used to
     * separate path elements).
     */
    public ResourceManager (String resourceRoot)
    {
        this(resourceRoot, ResourceManager.class.getClassLoader());
    }

    /**
     * Creates a resource manager with the specified class loader via
     * which to load classes. See {@link #ResourceManager(String)} for
     * further documentation.
     */
    public ResourceManager (String resourceRoot, ClassLoader loader)
    {
        _rootPath = resourceRoot;
        _loader = loader;

        // set up a URL handler so that things can be loaded via urls
        // with the 'resource' protocol
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run () {
                Handler.registerHandler(ResourceManager.this);
                return null;
            }
        });
    }

    /**
     * Initializes the bundle sets to be made available by this resource
     * manager.  Applications that wish to make use of resource bundles
     * should call this method after constructing the resource manager.
     *
     * @param resourceDir the base directory to which the paths in the
     * supplied configuration file are relative. If this is null, the
     * system property <code>resource_dir</code> will be used, if
     * available.
     * @param configPath the path (relative to the resource dir) of the
     * resource definition file.
     * @param initObs a bundle initialization observer to notify of
     * unpacking progress and success or failure, or <code>null</code> if
     * the caller doesn't care to be informed; note that in the latter
     * case, the calling thread will block until bundle unpacking is
     * complete.
     *
     * @exception IOException thrown if we are unable to read our resource
     * manager configuration.
     */
    public void initBundles (
        String resourceDir, String configPath, InitObserver initObs)
        throws IOException
    {
        // if the resource directory wasn't provided, we try to figure it
        // out for ourselves
        if (resourceDir == null) {
            try {
                // first look for the explicit system property
                resourceDir = System.getProperty("resource_dir");
                // if that doesn't work, fall back to the current directory
                if (resourceDir == null) {
                    resourceDir = System.getProperty("user.dir");
                }

            } catch (SecurityException se) {
                resourceDir = File.separator;
            }
        }

        // make sure there's a trailing slash
        if (!resourceDir.endsWith(File.separator)) {
            resourceDir += File.separator;
        }
        _rdir = new File(resourceDir);

        // load up our configuration
        Properties config = new Properties();
        try {
            config.load(new FileInputStream(new File(_rdir, configPath)));
        } catch (Exception e) {
            String errmsg = "Unable to load resource manager config " +
                "[rdir=" + _rdir + ", cpath=" + configPath + "]";
            Log.warning(errmsg + ".");
            Log.logStackTrace(e);
            throw new IOException(errmsg);
        }

        // resolve the configured resource sets
        ArrayList dlist = new ArrayList();
        Enumeration names = config.propertyNames();
        while (names.hasMoreElements()) {
            String key = (String)names.nextElement();
            if (!key.startsWith(RESOURCE_SET_PREFIX)) {
                continue;
            }
            String setName = key.substring(RESOURCE_SET_PREFIX.length());
            resolveResourceSet(setName, config.getProperty(key), dlist);
        }

        // if an observer was passed in, then we do not need to block
        // the caller
        final boolean[] shouldWait = new boolean[] { false };
        if (initObs == null) {
            // if there's no observer, we'll need to block the caller
            shouldWait[0] = true;
            initObs = new InitObserver() {
                public void progress (int percent, long remaining) {
                    if (percent >= 100) {
                        synchronized (this) {
                            // turn off shouldWait, in case we reached
                            // 100% progress before the calling thread even
                            // gets a chance to get to the blocking code, below
                            shouldWait[0] = false;
                            notify();
                        }
                    }
                }
                public void initializationFailed (Exception e) {
                    synchronized (this) {
                        shouldWait[0] = false;
                        notify();
                    }
                }
            };
        }

        // start a thread to unpack our bundles
        Unpacker unpack = new Unpacker(dlist, initObs);
        unpack.start();

        if (shouldWait[0]) {
            synchronized (initObs) {
                if (shouldWait[0]) {
                    try {
                        initObs.wait();
                    } catch (InterruptedException ie) {
                        Log.warning("Interrupted while waiting for bundles " +
                                    "to unpack.");
                    }
                }
            }
        }
    }

    /**
     * Given a path relative to the resource directory, the path is
     * properly jimmied (assuming we always use /) and combined with the
     * resource directory to yield a {@link File} object that can be used
     * to access the resource.
     */
    public File getResourceFile (String path)
    {
        if (!"/".equals(File.separator)) {
            path = StringUtil.replace(path, "/", File.separator);
        }
        return new File(_rdir, path);
    }

    /**
     * Checks to see if the specified bundle exists, is unpacked and is
     * ready to be used.
     */
    public boolean checkBundle (String path)
    {
        return new ResourceBundle(
            getResourceFile(path), true, true).isUnpacked();
    }

    /**
     * Resolve the specified bundle (the bundle file must already exist in
     * the appropriate place on the file system) and return it on the
     * specified result listener. Note that the result listener may be
     * notified before this method returns on the caller's thread if the
     * bundle is already resolved, or it may be notified on a brand new
     * thread if the bundle requires unpacking.
     */
    public void resolveBundle (String path, final ResultListener listener)
    {
        final ResourceBundle bundle =
            new ResourceBundle(getResourceFile(path), true, true);
        if (bundle.isUnpacked()) {
            if (bundle.sourceIsReady()) {
                listener.requestCompleted(bundle);
            } else {
                String errmsg = "Bundle initialization failed.";
                listener.requestFailed(new IOException(errmsg));
            }
            return;
        }

        // start a thread to unpack our bundles
        ArrayList list = new ArrayList();
        list.add(bundle);
        Unpacker unpack = new Unpacker(list, new InitObserver() {
            public void progress (int percent, long remaining) {
                if (percent == 100) {
                    listener.requestCompleted(bundle);
                }
            }
            public void initializationFailed (Exception e) {
                listener.requestFailed(e);
            }
        });
        unpack.start();
    }

    /**
     * Returns the class loader being used to load resources if/when there
     * are no resource bundles from which to load them.
     */
    public ClassLoader getClassLoader ()
    {
        return _loader;
    }

    /**
     * Fetches a resource from the local repository.
     *
     * @param path the path to the resource
     * (ie. "config/miso.properties"). This should not begin with a slash.
     *
     * @exception IOException thrown if a problem occurs locating or
     * reading the resource.
     */
    public InputStream getResource (String path)
        throws IOException
    {
        InputStream in = null;

        // first look for this resource in our default resource bundle
        for (int i = 0; i < _default.length; i++) {
            in = _default[i].getResource(path);
            if (in != null) {
                return in;
            }
        }

        // if we didn't find anything, try the classloader
        final String rpath = PathUtil.appendPath(_rootPath, path);
        in = (InputStream)AccessController.doPrivileged(new PrivilegedAction() {
            public Object run () {
                return _loader.getResourceAsStream(rpath);
            }
        });
        if (in != null) {
            return in;
        }

        // if we still haven't found it, we throw an exception
        String errmsg = "Unable to locate resource [path=" + path + "]";
        throw new FileNotFoundException(errmsg);
    }

    /**
     * Fetches the specified resource as an {@link ImageInputStream} and
     * one that takes advantage, if possible, of caching of unpacked
     * resources on the local filesystem.
     *
     * @exception FileNotFoundException thrown if the resource could not
     * be located in any of the bundles in the specified set, or if the
     * specified set does not exist.
     * @exception IOException thrown if a problem occurs locating or
     * reading the resource.
     */
    public ImageInputStream getImageResource (String path)
        throws IOException
    {
        // first look for this resource in our default resource bundle
        for (int i = 0; i < _default.length; i++) {
            File file = _default[i].getResourceFile(path);
            if (file != null) {
                return new FileImageInputStream(file);
            }
        }

        // if we didn't find anything, try the classloader
        final String rpath = PathUtil.appendPath(_rootPath, path);
        InputStream in = (InputStream)
            AccessController.doPrivileged(new PrivilegedAction() {
            public Object run () {
                return _loader.getResourceAsStream(rpath);
            }
        });
        if (in != null) {
            return new MemoryCacheImageInputStream(new BufferedInputStream(in));
        }

        // if we still haven't found it, we throw an exception
        String errmsg = "Unable to locate image resource [path=" + path + "]";
        throw new FileNotFoundException(errmsg);
    }

    /**
     * Returns an input stream from which the requested resource can be
     * loaded. <em>Note:</em> this performs a linear search of all of the
     * bundles in the set and returns the first resource found with the
     * specified path, thus it is not extremely efficient and will behave
     * unexpectedly if you use the same paths in different resource
     * bundles.
     *
     * @exception FileNotFoundException thrown if the resource could not
     * be located in any of the bundles in the specified set, or if the
     * specified set does not exist.
     * @exception IOException thrown if a problem occurs locating or
     * reading the resource.
     */
    public InputStream getResource (String rset, String path)
        throws IOException
    {
        // grab the resource bundles in the specified resource set
        ResourceBundle[] bundles = getResourceSet(rset);
        if (bundles == null) {
            throw new FileNotFoundException(
                "Unable to locate resource [set=" + rset +
                ", path=" + path + "]");
        }

        // look for the resource in any of the bundles
        int size = bundles.length;
        for (int ii = 0; ii < size; ii++) {
            InputStream instr = bundles[ii].getResource(path);
            if (instr != null) {
//                 Log.info("Found resource [rset=" + rset +
//                          ", bundle=" + bundles[ii].getSource().getPath() +
//                          ", path=" + path + ", in=" + instr + "].");
                return instr;
            }
        }

        throw new FileNotFoundException(
            "Unable to locate resource [set=" + rset + ", path=" + path + "]");
    }

    /**
     * Fetches the specified resource as an {@link ImageInputStream} and
     * one that takes advantage, if possible, of caching of unpacked
     * resources on the local filesystem.
     *
     * @exception FileNotFoundException thrown if the resource could not
     * be located in any of the bundles in the specified set, or if the
     * specified set does not exist.
     * @exception IOException thrown if a problem occurs locating or
     * reading the resource.
     */
    public ImageInputStream getImageResource (String rset, String path)
        throws IOException
    {
        // grab the resource bundles in the specified resource set
        ResourceBundle[] bundles = getResourceSet(rset);
        if (bundles == null) {
            throw new FileNotFoundException(
                "Unable to locate image resource [set=" + rset +
                ", path=" + path + "]");
        }

        // look for the resource in any of the bundles
        int size = bundles.length;
        for (int ii = 0; ii < size; ii++) {
            File file = bundles[ii].getResourceFile(path);
            if (file != null) {
//                 Log.info("Found image resource [rset=" + rset +
//                          ", bundle=" + bundles[ii].getSource() +
//                          ", path=" + path + ", file=" + file + "].");
                return new FileImageInputStream(file);
            }
        }

        String errmsg = "Unable to locate image resource [set=" + rset +
            ", path=" + path + "]";
        throw new FileNotFoundException(errmsg);
    }

    /**
     * Returns a reference to the resource set with the specified name, or
     * null if no set exists with that name. Services that wish to load
     * their own resources can allow the resource manager to load up a
     * resource set for them, from which they can easily load their
     * resources.
     */
    public ResourceBundle[] getResourceSet (String name)
    {
        return (ResourceBundle[])_sets.get(name);
    }

    /**
     * Loads up a resource set based on the supplied definition
     * information.
     */
    protected void resolveResourceSet (
        String setName, String definition, List dlist)
    {
        StringTokenizer tok = new StringTokenizer(definition, ":");
        ArrayList set = new ArrayList();

        while (tok.hasMoreTokens()) {
            String path = tok.nextToken().trim();
            ResourceBundle bundle =
                new ResourceBundle(getResourceFile(path), true, true);
            set.add(bundle);
            if (bundle.isUnpacked() && bundle.sourceIsReady()) {
                continue;
            }
            dlist.add(bundle);
        }

        // convert our array list into an array and stick it in the table
        ResourceBundle[] setvec = new ResourceBundle[set.size()];
        set.toArray(setvec);
        _sets.put(setName, setvec);

        // if this is our default resource bundle, keep a reference to it
        if (DEFAULT_RESOURCE_SET.equals(setName)) {
            _default = setvec;
        }
    }

    /** Used to unpack bundles on a separate thread. */
    protected static class Unpacker extends Thread
    {
        public Unpacker (List bundles, InitObserver obs)
        {
            _bundles = bundles;
            _obs = obs;
        }

        public void run ()
        {
            try {
                int count = 0;
                for (Iterator iter = _bundles.iterator(); iter.hasNext(); ) {
                    ResourceBundle bundle = (ResourceBundle)iter.next();
                    if (!bundle.sourceIsReady()) {
                        Log.warning("Bundle failed to initialize " +
                                    bundle + ".");
                    }
                    if (_obs != null) {
                        int pct = count*100/_bundles.size();
                        if (pct < 100) {
                            _obs.progress(pct, 1);
                        }
                    }
                    count++;
                }
                if (_obs != null) {
                    _obs.progress(100, 0);
                }

            } catch (Exception e) {
                if (_obs != null) {
                    _obs.initializationFailed(e);
                }
            }
        }

        protected List _bundles;
        protected InitObserver _obs;
    }

    /** The classloader we use for classpath-based resource loading. */
    protected ClassLoader _loader;

    /** The directory that contains our resource bundles. */
    protected File _rdir;

    /** The prefix we prepend to resource paths before attempting to load
     * them from the classpath. */
    protected String _rootPath;

    /** Our default resource set. */
    protected ResourceBundle[] _default = new ResourceBundle[0];

    /** A table of our resource sets. */
    protected HashMap _sets = new HashMap();

    /** The prefix of configuration entries that describe a resource
     * set. */
    protected static final String RESOURCE_SET_PREFIX = "resource.set.";

    /** The name of the default resource set. */
    protected static final String DEFAULT_RESOURCE_SET = "default";
}
