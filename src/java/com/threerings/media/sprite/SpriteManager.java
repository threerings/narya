//
// $Id: SpriteManager.java,v 1.13 2001/09/13 19:10:26 mdb Exp $

package com.threerings.media.sprite;

import java.awt.*;
import java.util.*;

import com.samskivert.util.Tuple;
import com.threerings.media.Log;

/**
 * The SpriteManager manages the sprites running about in the game.
 */
public class SpriteManager
{
    /**
     * Construct and initialize the SpriteManager object.
     */
    public SpriteManager ()
    {
        _sprites = new ArrayList();
	_notify = new ArrayList();
        _dirty = new DirtyRectList();
    }

    /**
     * Add a rectangle to the dirty rectangle list.
     *
     * @param rect the rectangle to add.
     */
    public void addDirtyRect (Rectangle rect)
    {
        _dirty.add(rect);
    }

    /**
     * When an animated view processes its dirty rectangles, it may
     * require an expansion of the dirty region which may in turn require
     * the invalidation of more sprites than were originally invalid. In
     * such cases, the animated view can call back to the sprite manager,
     * asking it to append the rectangles of the sprites that intersect a
     * particular region to the dirty rectangle list that it's processing.
     * A sprite's rectangle will only be appended if it's not already in
     * the list.
     *
     * @param rects the dirty rectangle list being processed by the
     * animated view.
     * @param bounds the bounds the intersection of which we have
     * interest.
     */
    public void invalidateIntersectingSprites (
        DirtyRectList rects, Polygon bounds)
    {
        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            if (sprite.intersects(bounds)) {
                if (rects.appendDirtyRect(sprite.getRenderedBounds())) {
                    Log.info("Expanded for: " + sprite);
                }
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
        // let the sprite know about us
        sprite.setSpriteManager(this);
        // add the sprite to our list
        _sprites.add(sprite);
        // and invalidate the sprite's original position
        sprite.invalidate();
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
        sprite.setSpriteManager(null);
    }

    /**
     * Return the list of dirty rects in screen pixel coordinates that
     * have been created by any sprites since the last time the dirty
     * rects were requested.
     *
     * @return the list of dirty rects.
     */
    public DirtyRectList getDirtyRects ()
    {
        // create a copy of the dirty rectangles
        DirtyRectList dirty = (DirtyRectList)_dirty.clone();

        // clear out the list
        _dirty.clear();

        // return the full original list
        return dirty;
    }

    /**
     * Render the sprites residing within the given polygon to the given
     * graphics context.
     *
     * @param gfx the graphics context.
     * @param bounds the bounding polygon.
     */
    public void renderSprites (Graphics2D gfx, Polygon bounds)
    {
        // TODO: optimize to store sprites based on quadrants they're
        // in (or somesuch), and sorted, so that we can more quickly
        // determine which sprites to draw.

        int size = _sprites.size();
        for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
            if (sprite.inside(bounds)) {
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
     * Called periodically by the tick tasks put on the AWT event
     * queue by the {@link AnimationManager}.  Handles moving about of
     * sprites and reporting of sprite collisions.
     */
    public void tick (long timestamp)
    {
	// tick all sprites
	tickSprites(timestamp);

	// re-sort the sprite list to account for potential new positions
	sortSprites();

	// notify sprite observers of any collisions
	handleCollisions();

	// notify sprite observers of any sprite events.  note that we
	// explicitly queue up events while ticking and checking for
	// collisions, and we only notify observers once we're
	// finished with all of those antics so that any actions the
	// observers may take, such as sprite removal, won't screw us
	// up elsewhere.
	handleSpriteEvents();
    }

    /**
     * Call <code>tick()</code> on all sprite objects to give them a
     * chance to move themselves about, change their display image,
     * and so forth.
     */
    protected void tickSprites (long timestamp)
    {
        int size = _sprites.size();
	for (int ii = 0; ii < size; ii++) {
            Sprite sprite = (Sprite)_sprites.get(ii);
	    sprite.tick(timestamp);
        }
    }

    /**
     * Sort the sprite list in order of ascending x-coordinate.
     */
    protected void sortSprites ()
    {
	Object[] sprites = new Sprite[_sprites.size()];
	_sprites.toArray(sprites);
	Arrays.sort(sprites, SPRITE_COMP);
	_sprites.clear();
	for (int ii = 0; ii < sprites.length; ii++) {
	    _sprites.add(sprites[ii]);
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

	// calculate the x-position of the right edge of the sprite
	// we're checking for collisions
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
	    }

	    if (obounds.intersects(bounds)) {
		sprite.notifyObservers(SpriteEvent.COLLIDED_SPRITE, other);
		other.notifyObservers(SpriteEvent.COLLIDED_SPRITE, sprite);
	    }
	}
    }

    /**
     * Notify all sprite observers of any sprite events that took
     * place during our most recent <code>tick()</code>.
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
		obs.spriteChanged(evt);
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

    /** The comparator used to sort sprites by horizontal position. */
    protected static final Comparator SPRITE_COMP = new SpriteComparator();

    /** The sprite objects we're managing. */
    protected ArrayList _sprites;

    /** The dirty rectangles created by sprites. */
    protected DirtyRectList _dirty;

    /** The list of pending sprite notifications. */
    protected ArrayList _notify;

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
