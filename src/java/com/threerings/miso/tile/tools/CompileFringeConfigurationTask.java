//
// $Id: CompileFringeConfigurationTask.java,v 1.3 2004/08/27 02:20:07 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
