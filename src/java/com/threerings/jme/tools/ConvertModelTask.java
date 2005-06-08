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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.jme.util.LoggingSystem;
import com.jmex.model.XMLparser.Converters.AseToJme;
import com.jmex.model.XMLparser.Converters.DummyDisplaySystem;
import com.jmex.model.XMLparser.Converters.FormatConverter;
import com.jmex.model.XMLparser.Converters.MaxToJme;
import com.jmex.model.XMLparser.Converters.Md2ToJme;
import com.jmex.model.XMLparser.Converters.Md3ToJme;
import com.jmex.model.XMLparser.Converters.ObjToJme;

/**
 * An ant task for converting various 3D model formats into JME's internal
 * format.
 */
public class ConvertModelTask extends Task
{
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    public void init () throws BuildException
    {
        // create a dummy display system which the converters need
        new DummyDisplaySystem();
        LoggingSystem.getLogger().setLevel(Level.WARNING);
    }

    public void execute () throws BuildException
    {
        for (int i = 0; i < _filesets.size(); i++) {
            FileSet fs = (FileSet)_filesets.get(i);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();

            for (int f = 0; f < srcFiles.length; f++) {
                File cfile = new File(fromDir, srcFiles[f]);
                String target = srcFiles[f];
                int didx = target.lastIndexOf(".");
                target = (didx == -1) ? target : target.substring(0, didx);
                target += ".jme";
                convertModel(cfile, new File(fromDir, target));
            }
        }
    }

    protected void convertModel (File source, File target)
    {
        if (source.lastModified() < target.lastModified()) {
            return;
        }

        System.out.println("Converting " + source + "...");
        String path = source.getPath().toLowerCase();
        String type = path.substring(path.lastIndexOf(".") + 1);

        // set up our converter
        FormatConverter convert = null;
        if (type.equals("obj")) {
            convert = new ObjToJme();
            try {
                convert.setProperty("mtllib", new URL("file:" + source));
            } catch (Exception e) {
                System.err.println("Failed to create material URL: " + e);
                return;
            }
        } else if (type.equals("3ds")) {
            convert = new MaxToJme();
        } else if (type.equals("md2")) {
            convert = new Md2ToJme();
        } else if (type.equals("md3")) {
            convert = new Md3ToJme();
        } else if (type.equals("ase")) {
            convert = new AseToJme();
        } else {
            System.err.println("Unknown model type '" + type + "'.");
            return;
        }

        // and do the deed
        try {
            BufferedOutputStream bout = new BufferedOutputStream(
                new FileOutputStream(target));
            BufferedInputStream bin = new BufferedInputStream(
                new FileInputStream(source));
            convert.convert(bin, bout);
            bout.close();
        } catch (IOException ioe) {
            System.err.println("Error converting '" + source +
                               "' to '" + target + "'.");
            ioe.printStackTrace(System.err);
        }
    }

    /** A list of filesets that contain tileset bundle definitions. */
    protected ArrayList _filesets = new ArrayList();
}
