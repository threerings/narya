//
// $Id: CompileFringeConfigurationTask.java 3099 2004-08-27 02:21:06Z mdb $
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

package com.threerings.jme.tile.tools;

import java.io.File;
import java.io.Serializable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.samskivert.io.PersistenceException;
import com.threerings.util.CompiledConfig;

import com.threerings.jme.tile.tools.xml.FringeConfigurationParser;

/**
 * Compiles a fringe configuration from XML format into binary format.
 */
public class CompileFringeConfigurationTask extends Task
{
    public void setConfig (File config)
    {
        _config = config;
    }

    public void setTarget (File target)
    {
        _target = target;
    }

    public void execute () throws BuildException
    {
        // make sure the config file exists
        if (!_config.exists()) {
            throw new BuildException(
                "Fringe configuration file not found [path=" + _config + "].");
        }

        FringeConfigurationParser parser = new FringeConfigurationParser();
        Serializable config;
        try {
            config = parser.parseConfig(_config);
        } catch (Exception e) {
            throw new BuildException("Failure parsing fringe config", e);
        }

        try {
            // and write it on out
            CompiledConfig.saveConfig(_target, config);
        } catch (Exception e) {
            throw new BuildException("Failure writing serialized config", e);
        }
    }

    protected File _config;
    protected File _target;
}
