//
// $Id: ResourceBundle.java,v 1.15 2003/05/20 16:29:36 mdb Exp $

package com.threerings.resource;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.samskivert.io.NestableIOException;
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
        this(source, false);
    }

    /**
     * Constructs a resource bundle with the supplied jar file.
     *
     * @param source a file object that references our source jar file.
     * @param delay if true, the bundle will wait until someone calls
     * {@link #sourceIsReady} before allowing access to its resources.
     */
    public ResourceBundle (File source, boolean delay)
    {
        _source = source;

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
     * Called by the resource manager once it has ensured that our
     * resource jar file is up to date and ready for reading.
     */
    public void sourceIsReady ()
    {
        // make a note of our source's last modification time
        _sourceLastMod = _source.lastModified();
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

        // compute the path to our temporary file
        String tpath = StringUtil.md5hex(_source.getPath() + "%" + path);
        File tfile = new File(getCacheDir(), tpath);
        if (tfile.exists() && (tfile.lastModified() > _sourceLastMod)) {
//             System.out.println("Using cached " + _source.getPath() +
//                                ":" + path);
            return tfile;
        }

        // make sure said resource exists in the first place
        JarEntry entry = _jarSource.getJarEntry(path);
        if (entry == null) {
//             Log.info("Couldn't locate " + path + " in " + _jarSource + ".");
            return null;
        }

//         System.out.println("Unpacking " + path);
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
        if (_sourceLastMod == 0) {
            return true;
        }

        try {
            if (_jarSource == null) {
                _jarSource = new JarFile(_source);
            }
            return false;

        } catch (IOException ioe) {
            throw new NestableIOException(
                "Failed to resolve resource bundle jar file " +
                "[file=" + _source + "]", ioe);
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

    /** The file from which we construct our jar file. */
    protected File _source;

    /** The last modified time of our source jar file. */
    protected long _sourceLastMod;

    /** The directory into which our contents are unpacked, if we are
     * unpacked. */
    protected File _unpackDir;

    /** The jar file from which we load resources. */
    protected JarFile _jarSource;

    /** A directory in which we temporarily unpack our resource files. */
    protected static File _tmpdir;
}
