//
// $Id: TileSetBundlerTask.java,v 1.1 2001/11/29 00:14:11 mdb Exp $

package com.threerings.media.tools.tile.bundle;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import com.threerings.media.tools.tile.MapFileTileSetIDBroker;

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
     * Sets the path to the bundle defintion file that we'll use to create
     * our tileset bundle.
     */
    public void setBundledef (File bundledef)
    {
        _bundledef = bundledef;
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
     * Sets the path to the bundle file that we'll be creating.
     */
    public void setTarget (File target)
    {
        _target = target;
    }

    /**
     * Performs the actual work of the task.
     */
    public void execute () throws BuildException
    {
        // make sure everything was set up properly
        ensureSet(_config, "Must specify the path to the bundler config " +
                  "file via the 'config' attribute.");
        ensureSet(_bundledef, "Must specify the path to the bundle " +
                  "definition file via the 'bundledef' attribute.");
        ensureSet(_mapfile, "Must specify the path to the tileset id map " +
                  "file via the 'mapfile' attribute.");
        ensureSet(_target, "Must specify the path to the target bundle " +
                  "file via the 'target' attribute.");

        try {
            // create a tileset bundler
            TileSetBundler bundler = new TileSetBundler(_config);

            // create our tileset id broker
            MapFileTileSetIDBroker broker =
                new MapFileTileSetIDBroker(_mapfile);

            // create the bundle
            bundler.createBundle(broker, _bundledef, _target);

            // commit changes to the tileset id mapping
            broker.commit();

        } catch (Exception e) {
            String errmsg = "Failure creating tileset bundle: " +
                e.getMessage();
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
    protected File _bundledef;
    protected File _mapfile;
    protected File _target;
}
