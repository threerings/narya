//
// $Id: CompiledConfig.java,v 1.1 2002/03/08 06:15:21 mdb Exp $

package com.threerings.yohoho.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

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
        throws IOException, ClassNotFoundException
    {
        ObjectInputStream oin = new ObjectInputStream(source);
        return (Serializable)oin.readObject();
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
