//
// $Id: CharacterManager.java,v 1.2 2001/10/22 18:21:41 shaper Exp $

package com.threerings.miso.scene;

import java.awt.Point;
import java.io.IOException;

import com.samskivert.util.Config;
import com.samskivert.util.HashIntMap;

import com.threerings.media.sprite.MultiFrameImage;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.Log;
import com.threerings.miso.scene.xml.XMLCharacterParser;
import com.threerings.miso.tile.TileUtil;
import com.threerings.miso.util.MisoUtil;

/**
 * The character manager provides facilities for constructing sprites
 * that are used to represent characters in a scene.
 */
public class CharacterManager
{
    /**
     * Construct the character manager.
     */
    public CharacterManager (Config config, TileManager tilemgr)
    {
        _tilemgr = tilemgr;

        // load character data descriptions
        String file = config.getValue(CHARFILE_KEY, DEFAULT_CHARFILE);
        try {
            new XMLCharacterParser().loadCharacters(file, _characters);
        } catch (IOException ioe) {
            Log.warning("Exception loading character descriptions " +
                        "[ioe=" + ioe + "].");
        }
    }

    /**
     * Returns a sprite representing the character described by the
     * given tile set id.
     *
     * @param the character tile set id.
     */
    public AmbulatorySprite getCharacter (int tsid)
    {
        CharacterInfo info = (CharacterInfo)_characters.get(tsid);
        if (info == null) {
            // no such character
            Log.warning("Unknown character requested [tsid=" + tsid + "].");
            return null;
        }

	MultiFrameImage[] anims = TileUtil.getAmbulatorySpriteFrames(
            _tilemgr, tsid, info.frameCount);

        AmbulatorySprite sprite = new AmbulatorySprite(0, 0, anims);
        sprite.setFrameRate(info.frameRate);
        sprite.setOrigin(info.origin.x, info.origin.y);

        return sprite;
    }

    /** The config key for the character description file. */
    protected static final String CHARFILE_KEY =
        MisoUtil.CONFIG_KEY + ".characters";

    /** The default character description file. */
    protected static final String DEFAULT_CHARFILE =
        "rsrc/config/miso/characters.xml";

    /** The hashtable of character info objects. */
    protected HashIntMap _characters = new HashIntMap();

    /** The tile manager. */
    protected TileManager _tilemgr;

    /**
     * A class that contains character description information for a
     * single character for use when constructing the character's
     * sprite.
     */
    public static class CharacterInfo
    {
        public int tsid;
        public int frameCount;
        public int frameRate;
        public Point origin = new Point();

        public String toString ()
        {
            return "[tsid=" + tsid + ", frameCount=" + frameCount +
                ", frameRate=" + frameRate + ", origin=" + origin + "]";
        }
    }
}
