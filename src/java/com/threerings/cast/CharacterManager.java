//
// $Id: CharacterManager.java,v 1.5 2001/10/26 01:17:21 shaper Exp $

package com.threerings.cast;

import java.awt.Point;
import java.io.IOException;

import com.threerings.media.tile.TileManager;

import com.threerings.cast.Log;
import com.threerings.cast.TileUtil;

/**
 * The character manager provides facilities for constructing sprites
 * that are used to represent characters in a scene.
 */
public class CharacterManager
{
    /**
     * Constructs the character manager.
     */
    public CharacterManager (TileManager tilemgr, ComponentRepository repo)
    {
        // save off our objects
        _tilemgr = tilemgr;
        _repo = repo;
    }

    /**
     * Returns a {@link CharacterSprite} representing the character
     * described by the given {@link CharacterDescriptor}, or
     * <code>null</code> if an error occurs.
     *
     * @param desc the character descriptor.
     */
    public CharacterSprite getCharacter (CharacterDescriptor desc)
    {
        // get the list of component ids
        int components[] = desc.getComponents();

        // TODO: here is where we'd iterate through all components
        // building up the full composite image of the sprite in each
        // orientation, standing and walking.  we punt for now, but
        // we'll revisit this soon enough.
        if (components.length == 0 || components.length > 1) {
            Log.warning("Invalid number of components " +
                        " [size=" + components.length + "].");
            return null;
        }

        CharacterComponent comp;
        try {
            // as noted above, no compositing for now.  note that when
            // we do composite, we probably will want to make sure all
            // components share a compatible component type.
            comp = _repo.getComponent(components[0]);
        } catch (Exception e) {
            Log.warning("Exception retrieving character component " +
                        "[e=" + e + "].");
            return null;
        }

        // instantiate the character sprite
        CharacterSprite sprite;
        try {
            sprite = (CharacterSprite)_charClass.newInstance();
        } catch (Exception e) {
            Log.warning("Failed to instantiate character sprite " +
                        "[e=" + e + "].");
            Log.logStackTrace(e);
            return null;
        }

        // populate the character sprite with its attributes
        ComponentType ctype = comp.getType();
        sprite.setFrames(comp.getFrames());
        sprite.setFrameRate(ctype.fps);
        sprite.setOrigin(ctype.origin.x, ctype.origin.y);

        return sprite;
    }

    /**
     * Instructs the character manager to construct instances of this
     * derived class of <code>CharacterSprite</code>.
     */
    public void setCharacterClass (Class charClass)
    {
        // sanity check
        if (!CharacterSprite.class.isAssignableFrom(charClass)) {
            Log.warning("Requested to use character class that does not " + 
                        "derive from CharacterSprite " +
                        "[class=" + charClass.getName() + "].");
            return;
        }

        // make a note of it
        _charClass = charClass;
    }

    /** The tile manager. */
    protected TileManager _tilemgr;

    /** The component repository. */
    protected ComponentRepository _repo;

    /** The character class to be created. */
    protected Class _charClass = CharacterSprite.class;
}
