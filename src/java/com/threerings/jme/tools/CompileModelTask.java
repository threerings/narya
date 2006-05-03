//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.jme.tools;

import java.io.File;

import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.jme.util.LoggingSystem;
import com.jmex.model.XMLparser.Converters.DummyDisplaySystem;

/**
 * An ant task for compiling 3D models defined in XML to fast-loading binary
 * files.
 */
public class CompileModelTask extends Task
{
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }
    
    public void init () throws BuildException
    {
        // create a dummy display system
        new DummyDisplaySystem();
        LoggingSystem.getLogger().setLevel(Level.WARNING);
    }
    
    public void execute ()
        throws BuildException
    {
        for (int ii = 0, nn = _filesets.size(); ii < nn; ii++) {
            FileSet fs = _filesets.get(ii);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();

            for (int f = 0; f < srcFiles.length; f++) {
                File source = new File(fromDir, srcFiles[f]);
                try {
                    CompileModel.compile(source);
                } catch (Exception e) {
                    System.err.println("Error compiling " + source + ": " + e);
                }
            }
        }
    }

    /** A list of filesets that contain XML models. */
    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();
}
