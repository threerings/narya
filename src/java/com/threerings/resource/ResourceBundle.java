//
// $Id: ResourceBundle.java,v 1.19 2003/07/14 22:18:18 ray Exp $

package com.threerings.resource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.samskivert.io.NestableIOException;
import com.samskivert.io.StreamUtil;
import com.samskivert.util.FileUtil;
import com.samskivert.util.StringUtil;

import org.apache.commons.io.StreamUtils;

/**
 * A resource bundle provides access to the resources in a jar file.
 */
public class ResourceBundle
{
    /**
     * Constructs a resource bundle with the supplied jar file.
     *
     * @param source a file object that references our source jar file.
     */
    public ResourceBundle (File source)
    {
        this(source, false, false);
    }

    /**
     * Constructs a resource bundle with the supplied jar file.
     *
     * @param source a file object that references our source jar file.
     * @param delay if true, the bundle will wait until someone calls
     * {@link #sourceIsReady} before allowing access to its resources.
     * @param unpack if true the bundle will unpack itself into a
     * temporary directory
     */
    public ResourceBundle (File source, boolean delay, boolean unpack)
    {
        _source = source;
        if (unpack) {
            String root = stripSuffix(source.getPath());
            _unpacked = new File(root + ".stamp");
            _cache = new File(root);
        }

        if (!delay) {
            sourceIsReady();
        }
    }

    /**
     * Returns the {@link File} from which resources are fetched for this
     * bundle.
     */
    public File getSource ()
    {
        return _source;
    }

    /**
     * @return true if the bundle is fully downloaded and successfully
     * unpacked.
     */
    public boolean isUnpacked ()
    {
        return (_source.exists() && (_unpacked != null) &&
            (_unpacked.lastModified() == _source.lastModified()));
    }

    /**
     * Called by the resource manager once it has ensured that our
     * resource jar file is up to date and ready for reading.
     */
    public void sourceIsReady ()
    {
        // make a note of our source's last modification time
        _sourceLastMod = _source.lastModified();

        // if we are unpacking files, the time to do so is now
        if (_unpacked != null && _unpacked.lastModified() != _sourceLastMod) {
            boolean resolved = false;
            try {
                resolved = !resolveJarFile();
            } catch (IOException ioe) {
                Log.warning("Failure resolving jar file '" + _source +
                            "': " + ioe + ".");
            }
            if (!resolved) {
                String errmsg = "Source ready but failed to resolve jar " +
                    "[source=" + _source + "]";
                throw new IllegalStateException(errmsg);
            }

            Log.info("Unpacking into " + _cache + "...");
            if (!_cache.exists()) {
                if (!_cache.mkdir()) {
                    Log.warning("Failed to create bundle cache directory '" +
                                _cache + "'.");
                    // we are hopelessly fucked
                    return;
                }
            } else {
                FileUtil.recursiveClean(_cache);
            }

            boolean failure = false;
            Enumeration entries = _jarSource.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry)entries.nextElement();
                File efile = new File(_cache, entry.getName());

                // if we're unpacking a normal jar file, it will have
                // special path entries that allow us to create our
                // directories first
                if (entry.isDirectory()) {
                    if (!efile.exists() && !efile.mkdir()) {
                        Log.warning("Failed to create bundle entry path '" +
                                    efile + "'.");
                        failure = true;
                    }
                    continue;
                }

                // but some do not, so we want to ensure that our
                // directories exist prior to getting down and funky
                File parent = new File(efile.getParent());
                if (!parent.exists() && !parent.mkdirs()) {
                    Log.warning("Failed to create bundle entry parent '" +
                                parent + "'.");
                    failure = true;
                    continue;
                }

                BufferedOutputStream fout = null;
                InputStream jin = null;
                try {
                    fout = new BufferedOutputStream(
                        new FileOutputStream(efile));
                    jin = _jarSource.getInputStream(entry);
                    StreamUtils.pipe(jin, fout);
                } catch (IOException ioe) {
                    Log.warning("Failure unpacking " + efile + ": " + ioe);
                    failure = true;
                } finally {
                    StreamUtil.close(jin);
                    StreamUtil.close(fout);
                }
            }

            // if everything unpacked smoothly, create our unpack stamp
            if (!failure) {
                try {
                    _unpacked.createNewFile();
                    if (!_unpacked.setLastModified(_sourceLastMod)) {
                        Log.warning("Failed to set last mod on stamp file '" +
                                    _unpacked + "'.");
                    }
                } catch (IOException ioe) {
                    Log.warning("Failure creating stamp file '" + _unpacked +
                                "': " + ioe + ".");
                }
            }
        }
    }

    /**
     * Fetches the named resource from this bundle. The path should be
     * specified as a relative, platform independent path (forward
     * slashes). For example <code>sounds/scream.au</code>.
     *
     * @param path the path to the resource in this jar file.
     *
     * @return an input stream from which the resource can be loaded or
     * null if no such resource exists.
     *
     * @exception IOException thrown if an error occurs locating the
     * resource in the jar file.
     */
    public InputStream getResource (String path)
        throws IOException
    {
        // unpack our resources into a temp directory so that we can load
        // them quickly and the file system can cache them sensibly
        File rfile = getResourceFile(path);
        return (rfile == null) ? null : new FileInputStream(rfile);
    }

    /**
     * Returns a file from which the specified resource can be loaded.
     * This method will unpack the resource into a temporary directory and
     * return a reference to that file.
     *
     * @param path the path to the resource in this jar file.
     *
     * @return a file from which the resource can be loaded or null if no
     * such resource exists.
     */
    public File getResourceFile (String path)
        throws IOException
    {
        if (resolveJarFile()) {
            return null;
        }

        // make sure said resource exists in the first place
        JarEntry entry = _jarSource.getJarEntry(path);
        if (entry == null) {
//             Log.info("Couldn't locate " + path + " in " + _jarSource + ".");
            return null;
        }

        // if we have been unpacked, return our unpacked file
        if (_cache != null) {
            return new File(_cache, path);
        }

        // otherwise, we unpack resources as needed into a temp directory
        String tpath = StringUtil.md5hex(_source.getPath() + "%" + path);
        File tfile = new File(getCacheDir(), tpath);
        if (tfile.exists() && (tfile.lastModified() > _sourceLastMod)) {
            return tfile;
        }

        // copy the resource into the temporary file
        BufferedOutputStream fout =
            new BufferedOutputStream(new FileOutputStream(tfile));
        InputStream jin = _jarSource.getInputStream(entry);
        StreamUtils.pipe(jin, fout);
        jin.close();
        fout.close();

        return tfile;
    }

    /**
     * Returns true if this resource bundle contains the resource with the
     * specified path. This avoids actually loading the resource, in the
     * event that the caller only cares to know that the resource exists.
     */
    public boolean containsResource (String path)
    {
        try {
            if (resolveJarFile()) {
                return false;
            }
            return (_jarSource.getJarEntry(path) != null);
        } catch (IOException ioe) {
            return false;
        }
    }

    /**
     * Returns a string representation of this resource bundle.
     */
    public String toString ()
    {
        try {
            resolveJarFile();
            return (_jarSource == null) ? "[file=" + _source + "]" :
                "[path=" + _jarSource.getName() +
                ", entries=" + _jarSource.size() + "]";

        } catch (IOException ioe) {
            return "[file=" + _source + ", ioe=" + ioe + "]";
        }
    }

    /**
     * Creates the internal jar file reference if we've not already got
     * it; we do this lazily so as to avoid any jar- or zip-file-related
     * antics until and unless doing so is required, and because the
     * resource manager would like to be able to create bundles before the
     * associated files have been fully downloaded.
     *
     * @return true if the jar file could not yet be resolved because we
     * haven't yet heard from the resource manager that it is ready for us
     * to access, false if all is cool.
     */
    protected boolean resolveJarFile ()
        throws IOException
    {
        // if we don't yet have our resource bundle's last mod time, we
        // have not yet been notified that it is ready
        if (_sourceLastMod == -1) {
            return true;
        }

        try {
            if (_jarSource == null) {
                _jarSource = new JarFile(_source);
            }
            return false;

        } catch (IOException ioe) {
            Log.warning("Failure creating jar file '" + _source + "'.");
            Log.logStackTrace(ioe);
            throw new NestableIOException(
                "Failed to resolve resource bundle jar file '" +
                _source + "'", ioe);
        }
    }

    /**
     * Returns the cache directory used for unpacked resources.
     */
    public static File getCacheDir ()
    {
        if (_tmpdir == null) {
            String tmpdir = System.getProperty("java.io.tmpdir");
            if (tmpdir == null) {
                Log.info("No system defined temp directory. Faking it.");
                tmpdir = System.getProperty("user.home");
            }
            setCacheDir(new File(tmpdir, ".narcache"));
        }
        return _tmpdir;
    }

    /**
     * Specifies the directory in which our temporary resource files
     * should be stored.
     */
    public static void setCacheDir (File tmpdir)
    {
        String rando = Long.toHexString((long)(Math.random() * Long.MAX_VALUE));
        _tmpdir = new File(tmpdir, rando);
        if (!_tmpdir.exists()) {
            Log.info("Creating narya temp cache directory '" + _tmpdir + "'.");
            _tmpdir.mkdirs();
        }

        // add a hook to blow away the temp directory when we exit
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run () {
                Log.info("Clearing narya temp cache '" + _tmpdir + "'.");
                FileUtil.recursiveDelete(_tmpdir);
            }
        });
    }

    /** Strips the .jar off of jar file paths. */
    protected static String stripSuffix (String path)
    {
        if (path.endsWith(".jar")) {
            return path.substring(0, path.length()-4);
        } else {
            // we have to change the path somehow
            return path + "-cache";
        }
    }

    /** The file from which we construct our jar file. */
    protected File _source;

    /** The last modified time of our source jar file. */
    protected long _sourceLastMod = -1;

    /** A file whose timestamp indicates whether or not our existing jar
     * file has been unpacked. */
    protected File _unpacked;

    /** A directory into which we unpack files from our bundle. */
    protected File _cache;

    /** The jar file from which we load resources. */
    protected JarFile _jarSource;

    /** A directory in which we temporarily unpack our resource files. */
    protected static File _tmpdir;
}
