//
// $Id: ResourceBundle.java,v 1.3 2002/07/19 20:12:23 shaper Exp $

package com.threerings.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.samskivert.io.NestableIOException;

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
        _source = source;
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
        resolveJarFile();
        // TBD: determine whether or not we need to convert the path into
        // a platform-dependent path if we're on Windows
        JarEntry entry = _jarSource.getJarEntry(path);
        InputStream stream = null;
        if (entry != null) {
            stream = _jarSource.getInputStream(entry);
        }
        return stream;
    }

    /**
     * Returns true if this resource bundle contains the resource with the
     * specified path. This avoids actually loading the resource, in the
     * event that the caller only cares to know that the resource exists.
     */
    public boolean containsResource (String path)
    {
        try {
            resolveJarFile();
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
            return "[path=" + _jarSource.getName() +
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
     */
    protected void resolveJarFile ()
        throws IOException
    {
        try {
            if (_jarSource == null) {
                _jarSource = new JarFile(_source);
            }

        } catch (IOException ioe) {
            throw new NestableIOException(
                "Failed to resolve resource bundle jar file " +
                "[file=" + _source + "]", ioe);
        }
    }

    /** The file from which we construct our jar file. */
    protected File _source;

    /** The jar file from which we load resources. */
    protected JarFile _jarSource;
}
