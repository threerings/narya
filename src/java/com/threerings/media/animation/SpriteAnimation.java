//
// $Id: SpriteAnimation.java,v 1.3 2003/04/30 00:45:02 mdb Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.sprite.PathObserver;
import com.threerings.media.util.Path;

public class SpriteAnimation extends Animation
    implements PathObserver
{
    /**
     * Constructs a sprite animation for the given sprite. The first time
     * the animation is ticked, the sprite will be added to the given
     * sprite manager and started along the supplied path.
     */
    public SpriteAnimation (SpriteManager spritemgr, Sprite sprite, Path path)
    {
        super(new Rectangle());

        // save things off
        _spritemgr = spritemgr;
        _sprite = sprite;
        _path = path;

        // set up our sprite business
        _sprite.addSpriteObserver(this);
    }

    /**
     * Derived classes can override tick if they want to end the animation
     * in some other way than the sprite completing a path.
     */
    public void tick (long timestamp)
    {
        // start the sprite moving on its path on our first tick
        if (_path != null) {
            _spritemgr.addSprite(_sprite);
            _sprite.move(_path);
            _path = null;
        }
    }    

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        // nothing for now
    }

    // documentation inherited from interface
    public void pathCancelled (Sprite sprite, Path path)
    {
        _finished = true;
    }

    // documentation inherited from interface
    public void pathCompleted (Sprite sprite, Path path, long when)
    {
        _finished = true;
    }

    // documentation inherited
    protected void didFinish ()
    {
        super.didFinish();

        _spritemgr.removeSprite(_sprite);
        _sprite = null;
    }

    /** The sprite associated with this animation. */
    protected Sprite _sprite;

    /** The sprite manager managing our sprite. */
    protected SpriteManager _spritemgr;

    /** The path along which we'll move our sprite. */
    protected Path _path;
}
