//
// $Id: EditorContext.java 7429 2003-04-01 02:19:34Z mdb $

package com.threerings.stage.tools.editor.util;

import java.util.List;

import com.threerings.media.image.ColorPository;
import com.threerings.media.tile.TileSetRepository;

import com.threerings.stage.util.StageContext;

public interface EditorContext extends StageContext
{
    /**
     * Return a reference to the tile set repository in use by the tile
     * manager.  This reference is valid for the lifetime of the
     * application.
     */
    public TileSetRepository getTileSetRepository ();

    /**
     * Returns a colorization repository for use by the editor.
     */
    public ColorPository getColorPository ();

    /**
     * Inserts all known scene types into the supplied list.
     */
    public void enumerateSceneTypes (List types);
}
