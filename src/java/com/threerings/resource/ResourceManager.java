//
// $Id: ResourceManager.java,v 1.2 2001/08/15 02:12:46 mdb Exp $

package com.threerings.resource;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * The resource manager is responsible for maintaining a repository of
 * resources that are synchronized with a remote source. This is
 * accomplished in the form of a set of jar files that contain resources
 * and that are updated from a remote resource repository via HTTP.
 */
public class ResourceManager
{
    /**
     * Temporary means by which to construct a resource manager that loads
     * resources from the specified source. The resource root is prepended
     * to any path that is requested and that fully qualified path is
     * searched for in the classpath.
     */
    public ResourceManager (String resourceRoot)
    {
        // keep track of our root path
        _rootPath = resourceRoot;

        // make root path end with a slash
        if (!_rootPath.endsWith("/")) {
            _rootPath = _rootPath + "/";
        }

        // use the classloader that loaded us
        _loader = getClass().getClassLoader();
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
        String rpath = _rootPath + path;
        InputStream in = _loader.getResourceAsStream(rpath);
        if (in == null) {
            String errmsg = "Unable to locate resource [path=" + rpath + "]";
            throw new FileNotFoundException(errmsg);
        }
        return in;
    }

    /**
     * Fetches the requested resource and loads its contents into a byte
     * array, which is returned. Note: this is hugely inefficient because
     * the data is copied twice while reading and then once entirely again
     * when a brand new byte array is returned for you, the caller (thanks
     * to the inflexibility of <code>ByteArrayOutputStream</code>). Anyone
     * reading a lot of resources should obtain an
     * <code>InputStream</code> and read the data directly into where it
     * needs to go.
     *
     * @exception IOException thrown if a problem occurs locating or
     * reading the resource.
     *
     * @see #getResource
     */
    public byte[] getResourceAsBytes (String path)
        throws IOException
    {
        InputStream in = getResource(path);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];

        // i loathe to while(1), but we need a non-trivial loop condition
        while (true) {
            int bytes = in.read(buffer, 0, buffer.length);
            if (bytes == 0) {
                throw new IOException("Read zero bytes!?");
            } else if (bytes < 0) {
                break;
            }
            out.write(buffer, 0, bytes);
        }

        return out.toByteArray();
    }

    public static void main (String[] args)
    {
        try {
            ResourceManager rmgr = new ResourceManager("rsrc");
            byte[] data = rmgr.getResourceAsBytes(
                "config/miso/miso.properties");
            System.out.println(new String(data));

        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    protected ClassLoader _loader;
    protected String _rootPath;
}
