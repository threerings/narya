//
// $Id: ResourceManager.java,v 1.37 2003/10/27 11:43:26 mdb Exp $

package com.threerings.resource;

import java.awt.EventQueue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import com.samskivert.util.ResultListener;
import com.samskivert.util.StringUtil;

import com.threerings.resource.DownloadManager.DownloadDescriptor;
import com.threerings.resource.DownloadManager.DownloadObserver;

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
 * the resource manager, providing the URL of a resource definition file
 * which describes these resource sets. The definition file will be loaded
 * and the resource bundles defined within will be loaded relative to the
 * resource definition URL.  The bundles will be cached in the user's home
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
     * Provides facilities for notifying an observer of resource bundle
     * download progress.
     */
    public interface BundleDownloadObserver
    {
        /**
         * If this method returns true the download observer callbacks
         * will be called on the AWT thread, allowing the observer to do
         * things like safely update user interfaces, etc. If false, it
         * will be called on a special download thread.
         */
        public boolean notifyOnAWTThread ();

        /**
         * Called when the resource manager is about to check for an
         * update of any of our resource sets.
         */
        public void checkingForUpdate ();

        /**
         * Called to inform the observer of ongoing progress toward
         * completion of the overall bundle downloading task.  The caller
         * is guaranteed to get at least one call reporting 100%
         * completion.
         *
         * @param percent the percent completion of the download.
         * @param remaining the estimated download time remaining in
         * seconds, or <code>-1</code> if the time can not yet be
         * determined.
         */
        public void downloadProgress (int percent, long remaining);

        /**
         * Called to inform the observer that the bundle patching process
         * has begun. This follows the download phase, but is optional.
         */
        public void patching ();

        /**
         * Called to inform the observer that the bundle unpacking process
         * has begun. This follows the download or patching phase.
         */
        public void unpacking ();

        /**
         * Called to indicate that we have validated and unpacked our
         * bundles and are fully ready to go. A call to {@link
         * #dowloadProgress} with 100 percent completion is guaranteed to
         * precede this call and optionally one to {@link #patching} and
         * {@link #unpacking}.
         */
        public void downloadComplete ();

        /**
         * Called if a failure occurs while checking for an update or
         * downloading all resource sets.
         */
        public void downloadFailed (Exception e);
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
        // keep track of our root path
        _rootPath = resourceRoot;

        // make root path end with a slash (not the platform dependent
        // file system separator character as resource paths are passed to
        // ClassLoader.getResource() which requires / as its separator)
        if (!_rootPath.endsWith("/")) {
            _rootPath = _rootPath + "/";
        }

        // use the classloader that loaded us
        _loader = getClass().getClassLoader();

        // set up a URL handler so that things can be loaded via
        // urls with the 'resource' protocol
        Handler.registerHandler(this);

        // sort our our default cache path
        _baseCachePath = "";
        try {
            // first check for an explicitly specified cache directory
            _baseCachePath = System.getProperty("narya.resource.cache_dir");
            // if that's null, try putting it into their home directory
            if (_baseCachePath == null) {
                _baseCachePath = System.getProperty("user.home");
            }
            if (!_baseCachePath.endsWith(File.separator)) {
                _baseCachePath += File.separator;
            }
            _baseCachePath += (CACHE_PATH + File.separator);
        } catch (SecurityException se) {
            Log.info("Can't obtain user.home system property. Probably " +
                     "won't be able to create our cache directory " +
                     "either. [error=" + se + "].");
        }
    }

    /**
     * Instructs the resource manager to store cached resources in the
     * specified directory. This must be called before {@link
     * #initBundles} if it is going to be called at all. If it is not
     * called, the default cache directory will be used.
     */
    public void setCachePath (String cachePath)
    {
        if (!cachePath.endsWith(File.separator)) {
            cachePath += File.separator;
        }
        _baseCachePath = cachePath;
    }

    /**
     * Initializes the bundle sets to be made available by this resource
     * manager.  Applications that wish to make use of resource bundles
     * should call this method after constructing the resource manager.
     *
     * @param resourceURL the base URL from which resources are loaded.
     * Relative paths specified in the resource definition file will be
     * loaded relative to this path. If this is null, the system property
     * <code>resource_url</code> will be used, if available.
     * @param configPath the path (relative to the resource URL) of the
     * resource definition file.
     * @param version the version of the resource bundles to be
     * downloaded.
     * @param downloadObs the bundle download observer to notify of
     * download progress and success or failure, or <code>null</code> if
     * the caller doesn't care to be informed; note that in the latter
     * case, the calling thread will block until bundle updating is
     * complete.
     *
     * @exception IOException thrown if we are unable to download our
     * resource manager configuration from the support resource URL.
     */
    public void initBundles (String resourceURL, String configPath,
                             String version, BundleDownloadObserver downloadObs)
        throws IOException
    {
        _version = version;

        // if the resource URL wasn't provided, we try to figure it out
        // for ourselves
        if (resourceURL == null) {
            try {
                // first look for the explicit system property
                resourceURL = System.getProperty("resource_url");

                // if that doesn't work, fall back to the current directory
                if (resourceURL == null) {
                    resourceURL = "file:" + System.getProperty("user.dir");
                }

            } catch (SecurityException se) {
                resourceURL = "file:" + File.separator;
            }
        }

        // make sure there's a slash at the end of the URL
        if (!resourceURL.endsWith("/")) {
            resourceURL += "/";
        }

        try {
            _rurl = new URL(resourceURL);
        } catch (MalformedURLException mue) {
            Log.warning("Invalid resource URL [url=" + resourceURL +
                        ", error=" + mue + "].");
            throw new IOException("Invalid resource url '" + resourceURL + "'");
        }

        // if no dynamic resource URL has been specified, use the normal
        // resource URL in its stead
        if (_drurl == null) {
            _drurl = _rurl;
        }

        Log.debug("Resource manager ready [rurl=" + _rurl +
                  ", drurl=" + _drurl + "].");

        // load up our configuration
        Properties config = new Properties();
        URL curl = null;
        try {
            if (configPath != null) {
                curl = new URL(_rurl, configPath);
                config.load(curl.openStream());
            }
        } catch (MalformedURLException mue) {
            Log.warning("Unable to construct config URL [resourceURL=" + _rurl +
                        ", configPath=" + configPath + ", error=" + mue + "].");
            String errmsg = "Invalid config url [resourceURL=" + _rurl +
                ", configPath=" + configPath + "]";
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

        // start the download, blocking if we've no observer
        DownloadManager dlmgr = new DownloadManager();
        if (downloadObs == null) {
            downloadBlocking(dlmgr, dlist);
        } else {
            downloadNonBlocking(dlmgr, dlist, downloadObs);
        }
    }

    /**
     * Configures the resource manager with a custom dynamic resource
     * URL. If it is not thusly configured, it will use the normal
     * resource URL for dynamic resources.
     */
    public void setDynamicResourceURL (String dynResURL)
    {
        // make sure there's a slash at the end of the URL
        if (!dynResURL.endsWith("/")) {
            dynResURL += "/";
        }

        try {
            _drurl = new URL(dynResURL);
        } catch (MalformedURLException mue) {
            Log.warning("Invalid dynamic resource URL " +
                        "[url=" + dynResURL + ", error=" + mue + "].");
        }
    }

    /**
     * Create the resource bundle for the specified set and path.
     */
    protected ResourceBundle createResourceBundle (String setName, String path)
    {
        createCacheDirectory(setName);
        File cfile = new File(genCachePath(setName, path));

        return new ResourceBundle(cfile, true, true);
    }

    /**
     * Checks to see if the specified dynamic bundle is already here
     * and ready to roll.
     */
    public boolean checkDynamicBundle (String path)
    {
        return createResourceBundle(_dnset, path).isUnpacked();
    }

    /**
     * Resolve the specified dynamic bundle and return it on the specified
     * result listener.
     */
    public void resolveDynamicBundle (String path, String version,
                                      final ResultListener listener)
    {
        URL burl;
        try {
            burl = new URL(_drurl, path);
        } catch (MalformedURLException mue) {
            listener.requestFailed(mue);
            return;
        }

        // note our dynamic bundle set
        _dnset = DYNAMIC_BUNDLE_SET + StringUtil.md5hex(_drurl.toString());

        final ResourceBundle bundle = createResourceBundle(_dnset, path);
        if (bundle.isUnpacked()) {
            if (bundle.sourceIsReady()) {
                listener.requestCompleted(bundle);
            } else {
                String errmsg = "Bundle initialization failed.";
                listener.requestFailed(new IOException(errmsg));
            }
            return;
        }

        // slap this on the list for retrieval or update by the download
        // manager
        ArrayList list = new ArrayList();
        list.add(new DownloadDescriptor(burl, bundle.getSource(), version));

        // TODO: There should only be one download manager for all dynamic
        // bundles, with each bundle waiting its turn to use it.
        DownloadObserver obs = new DownloadObserver() {
            public boolean notifyOnAWTThread () {
                return false;
            }

            public void resolvingDownloads () {
            }

            public void downloadProgress (int percent, long remaining) {
            }

            public void patching () {
            }

            public void postDownloadHook () {
            }

            public void downloadComplete ()
            {
                final boolean unpacked = bundle.sourceIsReady();
                EventQueue.invokeLater(new Runnable() {
                    public void run () {
                        if (unpacked) {
                            listener.requestCompleted(bundle);
                        } else {
                            String errmsg = "Bundle initialization failed.";
                            listener.requestFailed(new IOException(errmsg));
                        }
                    }
                });
            }

            public void downloadFailed (
                DownloadDescriptor desc, final Exception e)
            {
                Log.warning("Failed to download file " +
                            "[desc=" + desc + ", e=" + e + "].");
                EventQueue.invokeLater(new Runnable() {
                    public void run () {
                        listener.requestFailed(e);
                    }
                });
            }
        };

        DownloadManager dlmgr = new DownloadManager();
        dlmgr.download(list, true, obs);
    }

    /**
     * Downloads the files in the supplied download list, blocking the
     * calling thread until the download is complete or a failure has
     * occurred.
     */
    protected void downloadBlocking (DownloadManager dlmgr, List dlist)
    {
        // create an object to wait on while the download takes place
        final Object lock = new Object();

        // create the observer that will notify us when all is finished
        DownloadObserver obs = new DownloadObserver() {
            public boolean notifyOnAWTThread () {
                return false;
            }

            public void resolvingDownloads () {
                // nothing for now
            }

            public void downloadProgress (int percent, long remaining) {
                // nothing for now
            }

            public void patching () {
                // nothing for now
            }

            public void postDownloadHook () {
                bundlesDownloaded();
            }

            public void downloadComplete () {
                synchronized (lock) {
                    // wake things up as the download is finished
                    lock.notify();
                }
            }

            public void downloadFailed (DownloadDescriptor desc, Exception e) {
                Log.warning("Failed to download file " +
                            "[desc=" + desc + ", e=" + e + "].");
                synchronized (lock) {
                    // wake things up since we're fragile and so a
                    // single failure means all is booched
                    lock.notify();
                }
            }
        };

        synchronized (lock) {
            // pass the descriptors on to the download manager
            dlmgr.download(dlist, true, obs);

            try {
                // block until the download has completed
                lock.wait();
            } catch (InterruptedException ie) {
                Log.warning("Thread interrupted while waiting for download " +
                            "to complete [ie=" + ie + "].");
            }
        }
    }

    /**
     * Downloads the files in the supplied download list asynchronously,
     * notifying the download observer of ongoing progress.
     */
    protected void downloadNonBlocking (
        DownloadManager dlmgr, List dlist, final BundleDownloadObserver obs)
    {
        // pass the descriptors on to the download manager
        dlmgr.download(dlist, true, new DownloadObserver() {
            public boolean notifyOnAWTThread () {
                return obs.notifyOnAWTThread();
            }

            public void resolvingDownloads () {
                obs.checkingForUpdate();
            }

            public void downloadProgress (int percent, long remaining) {
                obs.downloadProgress(percent, remaining);
            }

            public void patching () {
                obs.patching();
            }

            public void postDownloadHook () {
                obs.unpacking();
                _unpacked = bundlesDownloaded();
            }

            public void downloadComplete () {
                if (_unpacked) {
                    obs.downloadComplete();
                } else {
                    String errmsg = "Bundle(s) failed initialization.";
                    obs.downloadFailed(new IOException(errmsg));
                }
            }

            public void downloadFailed (DownloadDescriptor desc, Exception e) {
                obs.downloadFailed(e);
            }

            protected boolean _unpacked;
        });
    }

    /**
     * Called when our resource bundle downloads have completed.
     *
     * @return true if we are fully operational, false if one or more
     * bundles failed to initialize.
     */
    protected boolean bundlesDownloaded ()
    {
        // let our bundles know that it's ok for them to access their
        // resource files
        Iterator iter = _sets.values().iterator();
        boolean unpacked = true;
        while (iter.hasNext()) {
            ResourceBundle[] bundles = (ResourceBundle[])iter.next();
            for (int ii = 0; ii < bundles.length; ii++) {
                unpacked = bundles[ii].sourceIsReady() && unpacked;
            }
        }
        return unpacked;
    }

    /**
     * Ensures that the cache directory for the specified resource set is
     * created.
     *
     * @return true if the directory was successfully created (or was
     * already there), false if we failed to create it.
     */
    protected boolean createCacheDirectory (String setName)
    {
        // compute the path to the top-level cache directory if we haven't
        // already been so configured
        if (_cachePath == null) {
            // start with the base cache path
            _cachePath = _baseCachePath;
            if (!createDirectory(_cachePath)) {
                return false;
            }

            // next incorporate the resource URL into the cache path so
            // that files fetched from different resource roots do not
            // overwrite one another
            _cachePath += StringUtil.md5hex(_rurl.toString()) + File.separator;
            if (!createDirectory(_cachePath)) {
                return false;
            }

            Log.debug("Caching bundles in '" + _cachePath + "'.");
        }

        // ensure that the set-specific cache directory exists
        return createDirectory(_cachePath + setName);
    }

    /**
     * Creates the specified directory if it doesn't already exist.
     *
     * @return true if directory was created (or existed), false if not.
     */
    protected boolean createDirectory (String path)
    {
        File cdir = new File(path);
        if (cdir.exists()) {
            if (!cdir.isDirectory()) {
                Log.warning("Cache dir exists but isn't a directory?! " +
                            "[path=" + path + "].");
                return false;
            }

        } else {
            if (!cdir.mkdirs()) {
                Log.warning("Unable to create cache dir. " +
                            "[path=" + path + "].");
                return false;
            }
        }

        return true;
    }

    /**
     * Generates the name of the bundle cache file given the name of the
     * resource set to which it belongs and the relative path URL.
     */
    protected String genCachePath (String setName, String resourcePath)
    {
        return _cachePath + setName + File.separator +
            StringUtil.replace(resourcePath, "/", "-");
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
            URL burl = null;

            try {
                burl = new URL(_rurl, path);

                // make sure the cache directory exists for this set
                createCacheDirectory(setName);

                // compute the path to the cache file for this bundle
                File cfile = new File(genCachePath(setName, path));

                // slap this on the list for retrieval or update by the
                // download manager
                dlist.add(new DownloadDescriptor(burl, cfile, _version));

                // finally, add the file that will be cached to the set as
                // a resource bundle
                set.add(new ResourceBundle(cfile, true, true));

            } catch (MalformedURLException mue) {
                Log.warning("Unable to create URL for resource " +
                            "[set=" + setName + ", path=" + path +
                            ", error=" + mue + "].");
            }
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
        String rpath = _rootPath + path;
        in = _loader.getResourceAsStream(rpath);
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
        String rpath = _rootPath + path;
        InputStream in = _loader.getResourceAsStream(rpath);
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

    /** The classloader we use for classpath-based resource loading. */
    protected ClassLoader _loader;

    /** The version of the resource bundles we'll be downloading. */
    protected String _version;

    /** The url via which we download our bundles. */
    protected URL _rurl;

    /** The url via which we download our dynamically generated bundles. */
    protected URL _drurl;

    /** The resource set we use for dynamic resources from a particular
     * dynamic bundle URL. */
    protected String _dnset;

    /** The prefix we prepend to resource paths before attempting to load
     * them from the classpath. */
    protected String _rootPath;

    /** The path to the directory that will contain our bundle cache. */
    protected String _baseCachePath;

    /** The path to our bundle cache directory. */
    protected String _cachePath;

    /** Our default resource set. */
    protected ResourceBundle[] _default = new ResourceBundle[0];

    /** A table of our resource sets. */
    protected HashMap _sets = new HashMap();

    /** The prefix of configuration entries that describe a resource
     * set. */
    protected static final String RESOURCE_SET_PREFIX = "resource.set.";

    /** The name of the default resource set. */
    protected static final String DEFAULT_RESOURCE_SET = "default";

    /** The name of our resource bundle cache directory. */
    protected static final String CACHE_PATH = ".narya";

    /** The name of the resource set where we store dynamic bundles. */
    protected static final String DYNAMIC_BUNDLE_SET = "_dynamic";
}
