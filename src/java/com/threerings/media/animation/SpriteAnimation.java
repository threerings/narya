//
// $Id: SpriteAnimation.java,v 1.1 2002/03/16 03:11:23 shaper Exp $

package com.threerings.media.animation;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import com.threerings.media.sprite.PathCompletedEvent;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.sprite.SpriteEvent;
import com.threerings.media.sprite.SpriteManager;
import com.threerings.media.sprite.SpriteObserver;

public class SpriteAnimation extends Animation
    implements SpriteObserver
{
    /**
     * Constructs a sprite animation for the given sprite and adds the
     * sprite to the given sprite manager.  If the sprite has had a path
     * set, the animation will finish when the sprite completes its path.
     */
    public SpriteAnimation (SpriteManager spritemgr, Sprite sprite)
    {
        super(new Rectangle());

        // save things off
        _spritemgr = spritemgr;
        _sprite = sprite;

        // set up our sprite business
        _sprite.addSpriteObserver(this);
        _spritemgr.addSprite(_sprite);
    }

    /**
     * Derived classes can override tick if they want to end the animation
     * in some other way than the sprite completing a path.
     */
    public void tick (long timestamp)
    {
        // nothing for now
    }    

    // documentation inherited
    public void paint (Graphics2D gfx)
    {
        // nothing for now
    }

    // documentation inherited
    public void handleEvent (SpriteEvent event)
    {
        if (event instanceof PathCompletedEvent) {
            _finished = true;
        }
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
}
