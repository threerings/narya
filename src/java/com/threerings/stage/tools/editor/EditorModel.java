//
// $Id: EditorModel.java 17780 2004-11-10 23:00:07Z ray $

package com.threerings.stage.tools.editor;

import java.util.ArrayList;

import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;

import com.threerings.util.DirectionCodes;

/**
 * The EditorModel class provides a holding place for storing,
 * modifying and retrieving data that is shared across the Editor
 * application and its myriad UI components.
 */
public class EditorModel
{
    /** Action mode constants. */
    public static final int ACTION_PLACE_TILE = 0;
    public static final int ACTION_EDIT_TILE = 1;
    public static final int ACTION_PLACE_PORTAL = 2;

    /** The number of actions. */
    public static final int NUM_ACTIONS = 3;

    /** Miso layer constants. */
    public static final int BASE_LAYER = 0;
    public static final int OBJECT_LAYER = 1;
    public static final String[] LAYER_NAMES = { "Base", "Object" };

    /** Prose descriptions of actions suitable for tool tip display. */
    public static final String[] TIP_ACTIONS = {
        "Place/Delete tiles.", "Edit object tiles.",
        "Create/Edit/Delete portal."
    };

    /** String translations for action identifiers. */
    public static final String[] CMD_ACTIONS = {
        "place_tile", "edit_tile", "place_portal"
    };

    public EditorModel (TileManager tilemgr)
    {
        _tilemgr = tilemgr;
        _tileSetId = _tileIndex = -1;
        _lnum = _mode = 0;
    }

    /**
     * Add an editor model listener.
     *
     * @param l the listener.
     */
    public void addListener (EditorModelListener l)
    {
        if (!_listeners.contains(l)) {
            _listeners.add(l);
        }
    }

    /**
     * Notify all model listeners that the editor model has changed.
     */
    protected void notifyListeners (int event)
    {
        int size = _listeners.size();
        for (int ii = 0; ii < size; ii++) {
            ((EditorModelListener)_listeners.get(ii)).modelChanged(event);
        }
    }

    /**
     * Returns whether the currently selected tile is valid.
     */
    public boolean isTileValid ()
    {
        return (_tile != null);
    }

    /**
     * Returns the current editor action mode.
     */
    public int getActionMode ()
    {
        return _mode;
    }

    /**
     * Returns the current scene layer index undergoing edit.
     */
    public int getLayerIndex ()
    {
        return _lnum;
    }

    /**
     * Returns the currently selected tile set.
     */
    public TileSet getTileSet ()
    {
        return _tileSet;
    }

    /**
     * Returns the currently selected tile set id.
     */
    public int getTileSetId ()
    {
        return _tileSetId;
    }

    /**
     * Returns the currently selected tile id within the selected tile
     * set.
     */
    public int getTileId ()
    {
        return _tileIndex;
    }

    /**
     * Returns the currently selected tile for placement within the
     * scene.
     */
    public Tile getTile ()
    {
        return _tile;
    }

    /**
     * Returns the fully qualified tile id of the currently selected tile.
     */
    public int getFQTileId ()
    {
        return _fqTileId;
    }

    /**
     * Marks the currently selected tile as invalid such that editing
     * when no tiles are available can be properly effected.
     */
    public void clearTile ()
    {
        _tileSet = null;
        _tileSetId = -1;
        _tileIndex = -1;
        _tile = null;
    }

    /**
     * Sets the editor action mode.  The specified mode should be one
     * of <code>CMD_ACTIONS</code>.
     */
    public void setActionMode (String cmd)
    {
        for (int ii = 0; ii < CMD_ACTIONS.length; ii++) {
            if (CMD_ACTIONS[ii].equals(cmd)) {
                _mode = ii;
                notifyListeners(EditorModelListener.ACTION_MODE_CHANGED);
                return;
            }
        }

        Log.warning("Attempt to set to unknown mode [cmd=" + cmd + "].");
    }

    /**
     * Sets the scene layer index undergoing edit.
     */
    public void setLayerIndex (int lnum)
    {
        if (lnum != _lnum) {
            _lnum = lnum;
            notifyListeners(EditorModelListener.LAYER_INDEX_CHANGED);
        }
    }

    /**
     * Sets the selected tile for placement within the scene.
     */
    public void setTile (TileSet set, int tileSetId, int tileIndex)
    {
        _tile = set.getTile(tileIndex);
        _tileSet = set;
        _tileSetId = tileSetId;
        _tileIndex = tileIndex;
        _fqTileId = TileUtil.getFQTileId(tileSetId, tileIndex);
        notifyListeners(EditorModelListener.TILE_CHANGED);
    }

    /**
     * Sets the tile id of the tile selected for placement within the
     * scene.
     */
    public void setTileId (int tileIndex)
    {
        setTile(_tileSet, _tileSetId, tileIndex);
    }

    /**
     * Sets the direction in which we should grip objects.
     */
    public void setObjectGripDirection (int direction)
    {
        _objectGrip = direction;
    }

    /**
     * Gets the direction in which we should grip objects.
     */
    public int getObjectGripDirection ()
    {
        return _objectGrip;
    }

    /**
     * Returns a string representation of the editor model.
     */
    public String toString ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[set=").append(_tileSet);
        buf.append(", tid=").append(_tileIndex);
        buf.append(", lnum=").append(_lnum);
        buf.append(", tile=").append(_tile);
        return buf.append("]").toString();
    }

    /** The currently selected action mode. */
    protected int _mode;

    /** The currently selected tileset. */
    protected TileSet _tileSet;

    /** The currently selected tileset id. */
    protected int _tileSetId;

    /** The currently selected tile id. */
    protected int _tileIndex;

    /** The fully qualified tile id of the currently selected tile. */
    protected int _fqTileId;

    /** The currently selected layer number. */
    protected int _lnum;

    /** The currently selected tile for placement in the scene. */
    protected Tile _tile;

    /** The model listeners. */
    protected ArrayList _listeners = new ArrayList();

    /** The tile manager. */
    protected TileManager _tilemgr;

    /** Direction (which corner) we grip an object by. */
    protected int _objectGrip = DirectionCodes.SOUTH;
}
