//
// $Id: CompileFringeConfigurationTask.java,v 1.2 2002/04/06 01:38:32 mdb Exp $

package com.threerings.miso.tile.tools;

import java.io.File;
import java.io.Serializable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.samskivert.io.PersistenceException;

import com.threerings.util.CompiledConfig;

import com.threerings.media.tile.tools.MapFileTileSetIDBroker;

import com.threerings.miso.tile.tools.xml.FringeConfigurationParser;

/**
 * Compile fringe configuration.
 */
public class CompileFringeConfigurationTask extends Task
{
    public void setTileSetMap (File tsetmap)
    {
        _tsetmap = tsetmap;
    }

    public void setFringeDef (File fringedef)
    {
        _fringedef = fringedef;
    }

    public void setTarget (File target)
    {
        _target = target;
    }

    public void execute () throws BuildException
    {
        // make sure the source file exists
        if (!_fringedef.exists()) {
            throw new BuildException("Fringe definition file not found " +
                                     "[path=" + _fringedef.getPath() + "].");
        }

        // set up the tileid broker
        MapFileTileSetIDBroker broker;
        try {
            broker = new MapFileTileSetIDBroker(_tsetmap);
        } catch (PersistenceException pe) {
            throw new BuildException("Couldn't set up tileset mapping " +
                "[path=" + _tsetmap.getPath() +
                ", error=" + pe.getCause() + "].");
        }

        FringeConfigurationParser parser = new FringeConfigurationParser(
            broker);
        Serializable config;
        try {
            config = parser.parseConfig(_fringedef);
        } catch (Exception e) {
            throw new BuildException("Failure parsing config definition", e);
        }

        try {
            // and write it on out
            CompiledConfig.saveConfig(_target, config);
        } catch (Exception e) {
            throw new BuildException("Failure writing serialized config", e);
        }
    }

    protected File _tsetmap;
    protected File _fringedef;
    protected File _target;
}
