//
// $Id: TileSetUtil.java 9371 2003-06-02 20:28:21Z mdb $

package com.threerings.stage.tools.editor.util;

import com.threerings.media.tile.TileSet;
import com.threerings.miso.tile.BaseTileSet;

import com.threerings.stage.tools.editor.Log;
import com.threerings.stage.tools.editor.EditorModel;

/**
 * Miscellaneous useful routines for working with lists of {@link TileSet}
 * and {@link BaseTileSet} objects.
 */
public class TileSetUtil
{
    /**
     * Returns the layer index of the layer for which this tileset
     * provides tiles.
     */
    public static int getLayerIndex (TileSet set)
    {
        if (set instanceof BaseTileSet) {
            return EditorModel.BASE_LAYER;
        } else {
            return EditorModel.OBJECT_LAYER;
        }
    }
}
