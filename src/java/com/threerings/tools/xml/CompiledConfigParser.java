//
// $Id: CompiledConfigParser.java,v 1.1 2002/03/08 06:15:21 mdb Exp $

package com.threerings.yohoho.tools.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;

import org.xml.sax.SAXException;
import org.apache.commons.digester.Digester;

import com.threerings.yohoho.util.CompiledConfig;
import com.threerings.yohoho.tools.CompiledConfigTask;

/**
 * An abstract base implementation of a parser that is used to compile
 * configuration definitions into config objects for use by the client and
 * server.
 *
 * @see CompiledConfig
 * @see CompiledConfigTask
 */
public abstract class CompiledConfigParser
{
    /**
     * Parses the supplied configuration file into a serializable
     * configuration object.
     */
    public Serializable parseConfig (File source)
        throws IOException, SAXException
    {
        Digester digester = new Digester();
        digester.setDebug(99);
        Serializable config = createConfigObject();
        addRules(digester);
        digester.push(config);
        digester.parse(new FileInputStream(source));
        return config;
    }

    /**
     * Creates the config object instance that will be populated during
     * the parsing process.
     */
    protected abstract Serializable createConfigObject ();

    /**
     * Adds the necessary digester rules for parsing the config object.
     */
    protected abstract void addRules (Digester digester);
}
