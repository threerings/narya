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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.threerings.jme.model.BoneNode;
import com.threerings.jme.model.Model;
import com.threerings.jme.model.ModelMesh;
import com.threerings.jme.model.ModelNode;
import com.threerings.jme.model.SkinMesh;
import com.threerings.jme.tools.xml.ModelParser;

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
    
    public void execute ()
        throws BuildException
    {
        for (int ii = 0, nn = _filesets.size(); ii < nn; ii++) {
            FileSet fs = _filesets.get(ii);
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            File fromDir = fs.getDir(getProject());
            String[] srcFiles = ds.getIncludedFiles();

            for (int f = 0; f < srcFiles.length; f++) {
                File cfile = new File(fromDir, srcFiles[f]);
                String target = srcFiles[f];
                int didx = target.lastIndexOf(".");
                target = (didx == -1) ? target : target.substring(0, didx);
                target += ".dat";
                compileModel(cfile, new File(fromDir, target));
            }
        }
    }
    
    protected void compileModel (File source, File target)
    {
        if (source.lastModified() < target.lastModified()) {
            return;
        }
        System.out.println("Compiling " + source + "...");
        
        ModelDef mdef;
        try {
            mdef = _mparser.parseModel(source.toString());
            
        } catch (Exception e) {
            System.err.println("Error parsing '" + source + "': " + e);
            return;
        }
        
        Model model = mdef.createModel("model");
        try {
            FileOutputStream fos = new FileOutputStream(target);
            new ObjectOutputStream(fos).writeObject(model);
            
        } catch (IOException e) {
            System.err.println("Error writing '" + target + "': " + e);
        }
    }
    
    /** A list of filesets that contain XML models. */
    protected ArrayList<FileSet> _filesets = new ArrayList<FileSet>();
    
    /** A parser for the model definitions. */
    protected ModelParser _mparser = new ModelParser();
}
