//
// $Id: CompiledConfigTask.java,v 1.3 2004/08/27 02:20:35 mdb Exp $
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

package com.threerings.tools;

import java.io.File;
import java.io.Serializable;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.threerings.tools.xml.CompiledConfigParser;
import com.threerings.util.CompiledConfig;

/**
 * Used to parse configuration information from an XML file and create the
 * serialized representation that is used by the client and server.
 */
public class CompiledConfigTask extends Task
{
    public void setParser (String parser)
    {
        _parser = parser;
    }

    public void setConfigdef (File configdef)
    {
        _configdef = configdef;
    }

    public void setTarget (File target)
    {
        _target = target;
    }

    public void execute () throws BuildException
    {
        // instantiate and sanity check the parser class
        Object pobj = null;
        try {
            Class pclass = Class.forName(_parser);
            pobj = pclass.newInstance();
        } catch (Exception e) {
            throw new BuildException("Error instantiating config parser", e);
        }
        if (!(pobj instanceof CompiledConfigParser)) {
            throw new BuildException("Invalid parser class: " + _parser);
        }

        // make sure the source file exists
        if (!_configdef.exists()) {
            String errmsg = "Config definition file not found " +
                "[path=" + _configdef.getPath() + "]";
            throw new BuildException(errmsg);
        }

        CompiledConfigParser parser = (CompiledConfigParser)pobj;
        Serializable config = null;

        try {
            // parse it on up
            config = parser.parseConfig(_configdef);
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

    protected File _configdef;
    protected File _target;
    protected String _parser;
}
