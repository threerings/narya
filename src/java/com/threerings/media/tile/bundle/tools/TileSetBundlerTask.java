//
// $Id: TileSetBundlerTask.java,v 1.9 2004/08/27 02:12:43 mdb Exp $
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

package com.threerings.media.tile.bundle.tools;

import java.io.File;
import java.util.ArrayList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import com.threerings.media.tile.tools.MapFileTileSetIDBroker;

/**
 * Ant task for creating tilset bundles.
 */
public class TileSetBundlerTask extends Task
{
    /**
     * Sets the path to the bundler configuration file that we'll use when
     * creating the bundle.
     */
    public void setConfig (File config)
    {
        _config = config;
    }

    /**
     * Sets the path to the tileset id mapping file we'll use when
     * creating the bundle.
     */
    public void setMapfile (File mapfile)
    {
        _mapfile = mapfile;
    }

    /**
     * Adds a nested &lt;fileset&gt; element.
     */
    public void addFileset (FileSet set)
    {
        _filesets.add(set);
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        // make sure everything was set up properly
        ensureSet(_config, "Must specify the path to the bundler config " +
                  "file via the 'config' attribute.");
        ensureSet(_mapfile, "Must specify the path to the tileset id map " +
                  "file via the 'mapfile' attribute.");

        File cfile = null;
        try {
            // create a tileset bundler
            TileSetBundler bundler = new TileSetBundler(_config);

            // create our tileset id broker
            MapFileTileSetIDBroker broker =
                new MapFileTileSetIDBroker(_mapfile);

            // deal with the filesets
            for (int i = 0; i < _filesets.size(); i++) {
                FileSet fs = (FileSet)_filesets.get(i);
                DirectoryScanner ds = fs.getDirectoryScanner(getProject());
                File fromDir = fs.getDir(getProject());
                String[] srcFiles = ds.getIncludedFiles();

                for (int f = 0; f < srcFiles.length; f++) {
                    cfile = new File(fromDir, srcFiles[f]);

                    // figure out the bundle file based on the definition
                    // file
                    String cpath = cfile.getPath();
                    if (!cpath.endsWith(".xml")) {
                        System.err.println("Can't infer bundle name from " +
                                           "bundle config name " +
                                           "[path=" + cpath + "].\n" +
                                           "Config file should end with .xml.");
                        continue;
                    }
                    String bpath =
                        cpath.substring(0, cpath.length()-4) + ".jar";
                    File bfile = new File(bpath);

                    // create the bundle
                    if (bundler.createBundle(broker, cfile, bfile)) {
                        System.out.println(
                            "Created bundle from '" + cpath + "'...");
                    } else {
                        System.out.println(
                            "Tileset bundle up to date '" + bpath + "'.");
                    }
                }
            }

            // commit changes to the tileset id mapping
            broker.commit();

        } catch (Exception e) {
            String errmsg = "Failure creating tileset bundle [source=" + cfile +
                "]: " + e.getMessage();
            throw new BuildException(errmsg, e);
        }
    }

    protected void ensureSet (Object value, String errmsg)
        throws BuildException
    {
        if (value == null) {
            throw new BuildException(errmsg);
        }
    }

    protected File _config;
    protected File _mapfile;

    /** A list of filesets that contain tileset bundle definitions. */
    protected ArrayList _filesets = new ArrayList();
}
