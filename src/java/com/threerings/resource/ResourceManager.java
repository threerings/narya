//
// $Id: ResourceManager.java,v 1.4 2001/11/20 00:23:32 mdb Exp $

package com.threerings.resource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;

import com.samskivert.Log;

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
 * <p> The resource manager can be provided with config properties at
 * construct time, or it can load them via {@link #getResource} with a
 * path of <code>config/resource/manager.properties</code>. The config
 * properties should contain resource set definitions for the default
 * resource set and for any named resource sets needed by the application.
 * An example configuration follows:
 *
 * <pre>
 * resource.set.default = rsrc/sets/misc
 * resource.set.tiles = rsrc/sets/tiles:/global/resources/tiles: \
 *                      /home/mdb/test_tiles.jar
 * resource.set.sounds = rsrc/sets/sounds:/global/resources/sounds
 * </pre>
 *
 * Platform-specific file and path separators should be used in the
 * resource set definitions as these are actual file paths. If a path
 * component starts with a file separator, it will be interpreted as an
 * absolute path, whereas if it doesn't, it will be interpreted as
 * relative to the application root that was supplied to the resource
 * manager at construct time.
 *
 * <p> All resource set definitions are prefixed with
 * <code>resource.set.</code> and all text following that string is
 * considered to be the name of the resource set. The resource set named
 * <code>default</code> is the default resource set and is the one that is
 * searched for resources is a call to {@link #getResource}.
 *
 * <p> Resource set definitions can contain directories or individual jar
 * files, the latter are simply added to the resource set; for the former,
 * all jar files in the specified directory (but not its subdirectories)
 * are added to the set. When a resource is loaded from a resource set,
 * the set is searched in the order that entries are specified in the
 * definition (the left-most entry first, and so on). Jar files in a
 * directory are added to the set (and thus, searched) in alphabetical
 * order.
 */
public class ResourceManager
{
    /**
     * Constructs a resource manager with the supplied application and
     * resource roots. The resource manager configuration is loaded via
     * the resource root (see class documentation for details).
     *
     * @param appRoot the path to the application root directory. This is
     * a platform dependent path and should contain separator characters
     * proper to the host platform. If null, this will be obtained via the
     * <code>application.root</code> system property.
     * @param resourceRoot the path to prepend to resource paths prior to
     * attempting to load them via the classloader. This is not a platform
     * dependent path.
     */
    public ResourceManager (String appRoot, String resourceRoot)
    {
        this(appRoot, resourceRoot, null);
    }

    /**
     * Constructs a resource manager with the supplied application and
     * resource roots.
     *
     * @param appRoot the path to the application root directory. This is
     * a platform dependent path and should contain separator characters
     * proper to the host platform.
     * @param resourceRoot the path to prepend to resource paths prior to
     * attempting to load them via the classloader. This is not a platform
     * dependent path.
     * @param config the configuration for this resource manager. See
     * class documentation for a description of the config properties.
     */
    public ResourceManager (
        String appRoot, String resourceRoot, Properties config)
    {
        // keep track of our root path
        _rootPath = resourceRoot;

        // make root path end with a slash (not the platform dependent
        // file system separator character as resource paths are passed to
        // ClassLoader.getResource() which requires / as its separator)
        if (!_rootPath.endsWith("/")) {
            _rootPath = _rootPath + "/";
        }

        // get our app root if we weren't provided with one
        if (appRoot == null) {
            appRoot = System.getProperty(APP_ROOT_PROPERTY);
            // if it's still null, we complain loudly
            if (appRoot == null) {
                Log.warning("No application root provided to resource " +
                            "manager. Assuming current working directory.");
                appRoot = "";
            }
        }

        // make the app root end with a file separator (unless we're
        // rolling with an empty app root)
        if ((appRoot.length() > 0) && !appRoot.endsWith(File.separator)) {
            appRoot = appRoot + File.separator;
        }

        // use the classloader that loaded us
        _loader = getClass().getClassLoader();

        // load up our configuration if it wasn't supplied by the caller
        try {
            if (config == null) {
                config = new Properties();
                config.load(getResource(CONFIG_PATH));
            }

        } catch (FileNotFoundException fnfe) {
            // nothing to worry about here, we aren't required to have a
            // configuration

        } catch (IOException ioe) {
            // complain if some other error occurs
            Log.warning("Error loading resource manager configuration " +
                        "[path=" + CONFIG_PATH + ", error=" + ioe + "].");
        }

        // load up any configured resource sets
        Enumeration names = config.propertyNames();
        while (names.hasMoreElements()) {
            String key = (String)names.nextElement();
            if (!key.startsWith(RESOURCE_SET_PREFIX)) {
                continue;
            }
            String setName = key.substring(RESOURCE_SET_PREFIX.length());
            resolveResourceSet(appRoot, setName, config.getProperty(key));
        }
    }

    /**
     * Loads up a resource set based on the supplied definition
     * information.
     */
    protected void resolveResourceSet (
        String appRoot, String name, String definition)
    {
        StringTokenizer tok =
            new StringTokenizer(definition, File.pathSeparator);
        ArrayList set = new ArrayList();

        while (tok.hasMoreTokens()) {
            // obtain the path and fully qualify it
            String path = tok.nextToken();
            if (!path.startsWith(File.separator)) {
                path = appRoot + path;
            }

            try {
                File efile = new File(path);

                // if this isn't a directory, we assume it's a jar file
                if (!efile.isDirectory()) {
                    set.add(new ResourceBundle(efile));
                    continue;
                }

                // it is a directory, so we have to add all of its entries
                // to the bundle
                File[] efiles = efile.listFiles(new FilenameFilter() {
                    public boolean accept (File dir, String filename) {
                        // only worry about .jar files
                        return filename.endsWith(".jar");
                    }
                });

                if (efiles == null) {
                    Log.warning("Failure enumerating jars in directory " +
                                "[path=" + path + "].");
                    continue;
                }

                // phew, we made it
                for (int i = 0; i < efiles.length; i++) {
                    set.add(new ResourceBundle(efiles[i]));
                }

            } catch (IOException ioe) {
                Log.warning("Error processing resource set entry " +
                            "[entry=" + path + ", error=" + ioe + "].");
            }
        }

        // convert our array list into an array and stick it in the table
        ResourceBundle[] setvec = new ResourceBundle[set.size()];
        set.toArray(setvec);
        _sets.put(name, setvec);

        // if this is our default resource bundle, keep a reference to it
        if (DEFAULT_RESOURCE_SET.equals(name)) {
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

    /** Our default resource set. */
    protected ResourceBundle[] _default = new ResourceBundle[0];

    /** A table of our resource sets. */
    protected HashMap _sets = new HashMap();

    /** The system property from which we load our application root. */
    protected static final String APP_ROOT_PROPERTY = "application.root";

    /** The path to the resource manager config file (which will be loaded
     * via the classloader). */
    protected static final String CONFIG_PATH =
        "config/resource/manager.properties";

    /** The prefix of configuration entries that describe a resource
     * set. */
    protected static final String RESOURCE_SET_PREFIX = "resource.set.";

    /** The name of the default resource set. */
    protected static final String DEFAULT_RESOURCE_SET = "default";
}
