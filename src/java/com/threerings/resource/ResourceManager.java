//
// $Id: ResourceManager.java,v 1.7 2002/01/16 03:00:06 mdb Exp $

package com.threerings.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.util.StreamUtils;
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
 * <p> The resource manager must be provided with the URL of a resource
 * definition file which describes these resource sets at construct
 * time. The definition file will be loaded and the resource bundles
 * defined within will be loaded relative to the resource definition URL.
 * The bundles will be cached in the user's home directory and only
 * reloaded when the source resources have been updated. The resource
 * definition file looks something like the following:
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
     * Constructs a resource manager which will load resources as
     * specified in the configuration file, the path to which is supplied
     * via <code>configPath</code>. If resource sets are not needed and
     * resources will only be loaded via the classpath, null may be passed
     * in <code>resourceURL</code> and <code>configPath</code>.
     *
     * @param resourceRoot the path to prepend to resource paths prior to
     * attempting to load them via the classloader. When resources are
     * bundled into the default resource bundle, they don't need this
     * prefix, but if they're to be loaded from the classpath, it's likely
     * that they'll live in some sort of <code>resources</code> directory
     * to isolate them from the rest of the files in the classpath. This
     * is not a platform dependent path (forward slash is always used to
     * separate path elements).
     * @param resourceURL the base URL from which resources are loaded.
     * Relative paths specified in the resource definition file will be
     * loaded relative to this path. If this is null, the system property
     * <code>resource_url</code> will be used, if available.
     * @param configPath the path (relative to the resource URL) of the
     * resource definition file.
     */
    public ResourceManager (
        String resourceRoot, String resourceURL, String configPath)
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

        URL rurl = null;
        try {
            rurl = new URL(resourceURL);
        } catch (MalformedURLException mue) {
            Log.warning("Invalid resource URL [url=" + resourceURL +
                        ", error=" + mue + "].");
        }

        // load up our configuration
        Properties config = loadConfig(rurl, configPath);

        // resolve the configured resource sets
        Enumeration names = config.propertyNames();
        while (names.hasMoreElements()) {
            String key = (String)names.nextElement();
            if (!key.startsWith(RESOURCE_SET_PREFIX)) {
                continue;
            }
            String setName = key.substring(RESOURCE_SET_PREFIX.length());
            resolveResourceSet(rurl, setName, config.getProperty(key));
        }
    }

    /**
     * Loads up the most recent version of the resource manager
     * configuration.
     */
    protected Properties loadConfig (URL resourceURL, String configPath)
    {
        Properties config = new Properties();
        URL curl = null;

        try {
            if (configPath != null) {
                curl = new URL(resourceURL, configPath);
                config.load(curl.openStream());
            }

        } catch (MalformedURLException mue) {
            Log.warning("Unable to construct config URL " +
                        "[resourceURL=" + resourceURL +
                        ", configPath=" + configPath +
                        ", error=" + mue + "].");

        } catch (IOException ioe) {
            // complain if some other error occurs
            Log.warning("Error loading resource manager configuration " +
                        "[url=" + curl + ", error=" + ioe + "].");
        }

        return config;
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
        // get the path to the top-level cache directory if we don't
        // already have it
        if (_cachePath == null) {
            try {
                String dir = System.getProperty("user.home");
                _cachePath = (dir + File.separator +
                              CACHE_PATH + File.separator);
            } catch (SecurityException se) {
                Log.info("Can't obtain user.home system property. Probably " +
                         "won't be able to create our cache directory " +
                         "either. [error=" + se + "].");
                _cachePath = "";
            }
        }

        // make sure the main cache directory exists
        if (!createDirectory(_cachePath)) {
            return false;
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
            if (!cdir.mkdir()) {
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
        URL resourceURL, String setName, String definition)
    {
        StringTokenizer tok = new StringTokenizer(definition, ":");
        ArrayList set = new ArrayList();

        while (tok.hasMoreTokens()) {
            String path = tok.nextToken().trim();
            URL burl = null;

            try {
                burl = new URL(resourceURL, path);
                Log.info("Resolving resource: " + burl);

                // make sure the cache directory exists for this set
                createCacheDirectory(setName);

                // compute the path to the cache file for this bundle
                File cfile = new File(genCachePath(setName, path));

                Log.info("Cached to " + cfile.getPath());

                // download the resource bundle from the specified URL
                InputStream in = burl.openStream();
                FileOutputStream out = new FileOutputStream(cfile);

                // pipe the input stream into the output stream
                StreamUtils.pipe(in, out);
                in.close();
                out.close();

                // finally add this newly cached file to the set as a
                // resource bundle
                set.add(new ResourceBundle(cfile));

            } catch (MalformedURLException mue) {
                Log.warning("Unable to create URL for resource " +
                            "[set=" + setName + ", path=" + path +
                            ", error=" + mue + "].");

            } catch (IOException ioe) {
                Log.warning("Error processing resource set entry " +
                            "[url=" + burl + ", error=" + ioe + "].");
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

    /** The prefix we prepend to resource paths before attempting to load
     * them from the classpath. */
    protected String _rootPath;

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
    protected static final String CACHE_PATH = ".naryarsrc";
}
