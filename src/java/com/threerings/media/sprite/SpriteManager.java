//
// $Id: SpriteManager.java,v 1.35 2002/07/02 23:15:53 shaper Exp $

package com.threerings.media.sprite;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.CollectionUtil;
import com.samskivert.util.SortableArrayList;
import com.samskivert.util.Tuple;

import com.threerings.media.Log;
import com.threerings.media.MediaConstants;
import com.threerings.media.RegionManager;

/**
 * The sprite manager manages the sprites running about in the game.
 */
public class SpriteManager
    implements MediaConstants
{
    /**
     * Construct and initialize the sprite manager.
     */
    public SpriteManager (RegionManager remgr)
    {
        _sprites = new SortableArrayList();
	_notify = new ArrayList();
        _remgr = remgr;
    }

    /**
     * When an animated view processes its dirty rectangles, it may
     * require an expansion of the dirty region which may in turn
     * require the invalidation of more sprites than were originally
     * invalid. In such cases, the animated view can call back to the
     * sprite manager, asking it to append the sprites that intersect
     * a particular region to the given list.
     *
     * @param list the list to fill with any intersecting sprites.
     * @param bounds the bounds the intersection of which we have
     * interest.
     */
    public void getIntersectingSprites (List list, Shape shape)
    {
        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            if (sprite.intersects(shape)) {
                list.add(sprite);
            }
        }
    }

    /**
     * When an animated view is determining what entity in its view is
     * under the mouse pointer, it may require a list of sprites that are
     * "hit" by a particular pixel. The sprites' bounds are first checked
     * and sprites with bounds that contain the supplied point are further
     * checked for a non-transparent at the specified location.
     *
     * @param list the list to fill with any intersecting sprites.
     * @param x the x (screen) coordinate to be checked.
     * @param y the y (screen) coordinate to be checked.
     */
    public void getHitSprites (List list, int x, int y)
    {
        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            if (sprite.hitTest(x, y)) {
                list.add(sprite);
            }
        }
    }

    /**
     * Add a sprite to the set of sprites managed by this manager.
     *
     * @param sprite the sprite to add.
     */
    public void addSprite (Sprite sprite)
    {
        // initialize the sprite
        sprite.init(this);
        // add the sprite to our list
        _sprites.add(sprite);
        // and invalidate the sprite's original position
        sprite.invalidate();
    }

    /**
     * Returns a list of all sprites registered with the sprite manager.
     * The returned list is immutable, sprites should be added or removed
     * using {@link #addSprite} or {@link #removeSprite}.
     */
    public List getSprites ()
    {
        return Collections.unmodifiableList(_sprites);
    }

    /**
     * Removes the specified sprite from the set of sprites managed by
     * this manager.
     *
     * @param sprite the sprite to remove.
     */
    public void removeSprite (Sprite sprite)
    {
        // invalidate the current sprite position
        sprite.invalidate();
        // remove the sprite from our list
        _sprites.remove(sprite);
        // clear out our manager reference
        sprite.shutdown();
    }

    /**
     * Clears all sprites from the sprite manager. This does not
     * invalidate their vacated bounds because it is assumed that either
     * the whole view will be repainted after this or it is going away
     * entirely. The sprites will be {@link Sprite#shutdown}, however.
     */
    public void clearSprites ()
    {
        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            sprite.shutdown();
        }
        _sprites.clear();
    }

    /**
     * Provides access to the region manager that the sprite manager is
     * using to collect invalid regions every frame. This should generally
     * only be used by sprites that want to invalidate themselves.
     */
    public RegionManager getRegionManager ()
    {
        return _remgr;
    }

    /**
     * Render to the given graphics context the sprites intersecting the
     * given shape and residing in the specified layer.
     *
     * @param layer the layer to render; one of {@link #FRONT}, {@link
     * #BACK}, or {@link #ALL}.  The front layer contains all sprites with
     * a positive render order; the back layer contains all sprites with a
     * negative render order; all, both.
     * @param bounds the bounding shape.
     */
    public void renderSprites (Graphics2D gfx, int layer, Shape bounds)
    {
        // TODO: optimize to store sprites based on quadrants they're in
        // (or somesuch), and sorted, so that we can more quickly
        // determine which sprites to draw.

        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            int order = sprite.getRenderOrder();
            if (((layer == ALL) ||
                 (layer == FRONT && order >= 0) ||
                 (layer == BACK && order < 0)) &&
                sprite.intersects(bounds)) {
                sprite.paint(gfx);
            }
        }
    }

    /**
     * Render the sprite paths to the given graphics context.
     *
     * @param gfx the graphics context.
     */
    public void renderSpritePaths (Graphics2D gfx)
    {
        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
	    sprite.paintPath(gfx);
        }
    }

    /**
     * Must be called every frame so that the sprites can be properly
     * updated. Normally a sprite manager is used in conjunction with an
     * animated panel which case this is called automatically.
     */
    public void tick (long tickStamp)
    {
	// tick all sprites
	tickSprites(tickStamp);

	// re-sort the sprite list to account for potential new positions
	_sprites.sort(SPRITE_COMP);

//         // notify sprite observers of any collisions
//         handleCollisions();

	// notify sprite observers of any sprite events.  note that we
	// explicitly queue up events while ticking and checking for
	// collisions, and we only notify observers once we're
	// finished with all of those antics so that any actions the
	// observers may take, such as sprite removal, won't screw us
	// up elsewhere.
	handleSpriteEvents();
    }

    /**
     * If the sprite manager is paused for some length of time, it should
     * be fast forwarded by the appropriate number of milliseconds.  This
     * allows sprites to smoothly pick up where they left off rather than
     * abruptly jumping into the future, thinking that some outrageous
     * amount of time passed since their last tick.
     */
    public void fastForward (long timeDelta)
    {
        int size = _sprites.size();
	for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
	    sprite.fastForward(timeDelta);
        }
    }

    /**
     * Call {@link Sprite#tick} on all sprite objects to give them a
     * chance to move themselves about, change their display image,
     * generate dirty regions and so forth.
     */
    protected void tickSprites (long tickStamp)
    {
        int size = _sprites.size();
	for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
	    sprite.tick(tickStamp);
        }
    }

    /**
     * Check all sprites for collisions with others and inform any
     * sprite observers.
     */
    protected void handleCollisions ()
    {
	// gather a list of all sprite collisions
	int size = _sprites.size();
	for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
	    checkCollisions(ii, size, sprite);
	}
    }

    /**
     * Check a sprite for collision with any other sprites in the
     * sprite list and notify the sprite observers associated with any
     * sprites that do indeed collide.
     *
     * @param idx the starting sprite index.
     * @param size the total number of sprites.
     * @param sprite the sprite to check against other sprites for
     * collisions.
     */
    protected void checkCollisions (int idx, int size, Sprite sprite)
    {
	// TODO: make this handle quickly moving objects that may pass
	// through each other.

	// if we're the last sprite we know we've already handled any
	// collisions
	if (idx == (size - 1)) {
	    return;
	}

	// calculate the x-position of the right edge of the sprite we're
	// checking for collisions
	Rectangle bounds = sprite.getBounds();
	int edgeX = bounds.x + bounds.width;

	for (int ii = (idx + 1); ii < size; ii++) {
	    Sprite other = (Sprite)_sprites.get(ii);
	    Rectangle obounds = other.getBounds();
	    if (obounds.x > edgeX) {
		// since sprites are stored in the list sorted by
		// ascending x-position, we know this sprite and any
		// other sprites farther on in the list can't possibly
		// intersect with the sprite we're checking, so we're
		// done.
                return;

	    } else if (obounds.intersects(bounds)) {
		sprite.notifyObservers(new CollisionEvent(sprite, other));
	    }
	}
    }

    /**
     * Notify all sprite observers of any sprite events that took place
     * during our most recent <code>tick()</code>.
     */
    protected void handleSpriteEvents ()
    {
	int size = _notify.size();
	for (int ii = 0; ii < size; ii++) {
	    Tuple tup = (Tuple)_notify.remove(0);
	    ArrayList observers = (ArrayList)tup.left;
	    SpriteEvent evt = (SpriteEvent)tup.right;

	    int osize = observers.size();
	    for (int jj = 0; jj < osize; jj++) {
		SpriteObserver obs = (SpriteObserver)observers.get(jj);
		obs.handleEvent(evt);
	    }
	}
    }

    /**
     * Called by {@link Sprite#notifyObservers} to note that the
     * sprite's observers should be informed of a sprite event once
     * the current <code>tick()</code> is complete.
     *
     * @param observers the list of sprite observers.
     * @param event the sprite event to notify the observers of.
     */
    protected void notifySpriteObservers (ArrayList observers,
					  SpriteEvent event)
    {
	// throw it on the list of events for later
	_notify.add(new Tuple(observers, event));
    }

    /** The sprite objects we're managing. */
    protected SortableArrayList _sprites;

    /** The list of pending sprite notifications. */
    protected ArrayList _notify;

    /** Used to accumulate dirty regions. */
    protected RegionManager _remgr;

    /** The comparator used to sort sprites by horizontal position. */
    protected static final Comparator SPRITE_COMP = new SpriteComparator();

    /** Used to sort sprites. */
    protected static class SpriteComparator implements Comparator
    {
	public int compare (Object o1, Object o2)
	{
	    Sprite s1 = (Sprite)o1;
	    Sprite s2 = (Sprite)o2;
	    return (s2.getX() - s1.getX());
	}

	public boolean equals (Object obj)
	{
	    return (obj == this);
	}
    }
}
