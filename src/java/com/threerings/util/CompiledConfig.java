//
// $Id: CompiledConfig.java,v 1.3 2002/04/02 01:55:26 mdb Exp $

package com.threerings.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.samskivert.io.NestableIOException;

/**
 * Used to load and store compiled configuration data (generally XML files
 * that are parsed into Java object models and then serialized for rapid
 * and simple access on the client and server).
 */
public class CompiledConfig
{
    /**
     * Unserializes a configuration object from the supplied input stream.
     */
    public static Serializable loadConfig (InputStream source)
        throws IOException
    {
        try {
            ObjectInputStream oin = new ObjectInputStream(source);
            return (Serializable)oin.readObject();
        } catch (ClassNotFoundException cnfe) {
            String errmsg = "Unknown config class";
            throw new NestableIOException(errmsg, cnfe);
        }
    }

    /**
     * Serializes the supplied configuration object to the specified file
     * path.
     */
    public static void saveConfig (File target, Serializable config)
        throws IOException
    {
        FileOutputStream fout = new FileOutputStream(target);
        ObjectOutputStream oout = new ObjectOutputStream(fout);
        oout.writeObject(config);
        oout.close();
    }
}
