//
// $Id$
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

package com.threerings.media;

import java.io.IOException;
import java.util.Properties;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.samskivert.util.ConfigUtil;
import com.samskivert.util.LRUHashMap;
import com.samskivert.util.StringUtil;

import com.threerings.media.image.ImageUtil;

import com.threerings.media.tile.TileIcon;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;

/**
 * Manages the creation of icons from tileset images. The icon manager is
 * provided with a configuration file, which maps icon set identifiers to
 * uniform tilesets and provides the metric information for said tilesets.
 * UI code can subsequently request icons from the icon manager based on
 * icon set identifier and index.
 *
 * <p> The configuration might look like the following:
 *
 * <pre>
 * arrows.path = /rsrc/media/icons/arrows.png
 * arrows.metrics = 20, 25  # icons that are 20 pixels wide and 25 pixels tall
 *
 * smileys.path = /rsrc/media/icons/smileys.png
 * smileys.metrics = 16, 16  # icons that are 16 pixels square
 * </pre>
 *
 * A user could then request an <code>arrows</code> icon like so:
 *
 * <pre>
 * Icon icon = iconmgr.getIcon("arrows", 2);
 * </pre>
 */
public class IconManager
{
    /**
     * Creates an icon manager that will obtain tilesets from the supplied
     * tile manager and which will load its configuration information from
     * the specified properties file.
     *
     * @param tmgr the tile manager to use when fetching tilesets.
     * @param configPath the path (relative to the classpath) from which
     * the icon manager configuration can be loaded.
     *
     * @exception IOException thrown if an error occurs loading the
     * configuration file.
     */
    public IconManager (TileManager tmgr, String configPath)
        throws IOException
    {
        this(tmgr, ConfigUtil.loadProperties(configPath));
    }

    /**
     * Creates an icon manager that will obtain tilesets from the supplied
     * tile manager and which will read its configuration information from
     * the supplied properties file.
     */
    public IconManager (TileManager tmgr, Properties config)
    {
        // save these for later
        _tilemgr = tmgr;
        _config = config;
    }

    /**
     * If icon images should be loaded from a set of resource bundles
     * rather than the classpath, that set can be set here.
     */
    public void setSource (String resourceSet)
    {
        _rsrcSet = resourceSet;
    }

    /**
     * Fetches the icon with the specified index from the named icon set.
     */
    public Icon getIcon (String iconSet, int index)
    {
        try {
            // see if the tileset is already loaded
            TileSet set = (TileSet)_icons.get(iconSet);

            // load it up if not
            if (set == null) {
                String path = _config.getProperty(iconSet + PATH_SUFFIX);
                if (StringUtil.blank(path)) {
                    throw new Exception("No path specified for icon set");
                }

                String metstr = _config.getProperty(iconSet + METRICS_SUFFIX);
                if (StringUtil.blank(metstr)) {
                    throw new Exception("No metrics specified for icon set");
                }

                int[] metrics = StringUtil.parseIntArray(metstr);
                if (metrics == null || metrics.length != 2) {
                    throw new Exception("Invalid icon set metrics " +
                                        "[metrics=" + metstr + "]");
                }

                // load up the tileset
                if (_rsrcSet == null) {
                    set = _tilemgr.loadTileSet(
                        path, metrics[0], metrics[1]);
                } else {
                    set = _tilemgr.loadTileSet(
                        _rsrcSet, path, metrics[0], metrics[1]);
                }

                // cache it
                _icons.put(iconSet, set);
            }

            // fetch the appropriate image and create an image icon
            return new TileIcon(set.getTile(index));

        } catch (Exception e) {
            Log.warning("Unable to load icon [iconSet=" + iconSet +
                        ", index=" + index + ", error=" + e + "].");
        }

        // return an error icon
        return new ImageIcon(ImageUtil.createErrorImage(32, 32));
    }

    /** The tile manager we use to load tilesets. */
    protected TileManager _tilemgr;

    /** Our configuration information. */
    protected Properties _config;

    /** The resource bundle from which we load icon images, or null if
     * they should be loaded from the classpath. */
    protected String _rsrcSet;

    /** A cache of our icon tilesets. */
    protected LRUHashMap _icons = new LRUHashMap(ICON_CACHE_SIZE);
    
    /** The suffix we append to an icon set name to obtain the tileset
     * image path configuration parameter. */
    protected static final String PATH_SUFFIX = ".path";

    /** The suffix we append to an icon set name to obtain the tileset
     * metrics configuration parameter. */
    protected static final String METRICS_SUFFIX = ".metrics";

    /** The maximum number of icon tilesets that may be cached at once. */
    protected static final int ICON_CACHE_SIZE = 10;
}
