//
// $Id: AbstractMediaManager.java,v 1.3 2002/11/26 02:47:04 shaper Exp $

package com.threerings.media;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import java.util.ArrayList;
import java.util.Comparator;

import com.samskivert.util.ObserverList;
import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;
import com.samskivert.util.Tuple;

/**
 * Manages, ticks, and paints {@link AbstractMedia}.
 */
public abstract class AbstractMediaManager
    implements MediaConstants
{
    /**
     * Default constructor.
     */
    public AbstractMediaManager (RegionManager remgr)
    {
        _remgr = remgr;
    }

    /**
     * Returns region manager in use by this manager.
     */
    public RegionManager getRegionManager ()
    {
        return _remgr;
    }

    /**
     * Renders all registered media in the given layer that intersect
     * the supplied clipping rectangle to the given graphics context.
     *
     * @param layer the layer to render; one of {@link #FRONT}, {@link
     * #BACK}, or {@link #ALL}.  The front layer contains all animations
     * with a positive render order; the back layer contains all
     * animations with a negative render order; all, both.
     */
    public void renderMedia (Graphics2D gfx, int layer, Shape clip)
    {
        for (int ii=0, nn=_media.size(); ii < nn; ii++) {
            AbstractMedia media = (AbstractMedia) _media.get(ii);
            int order = media.getRenderOrder();
            try {
                if (((layer == ALL) ||
                     (layer == FRONT && order >= 0) ||
                     (layer == BACK && order < 0)) &&
                     clip.intersects(media.getBounds())) {
                    media.paint(gfx);
                }

            } catch (Exception e) {
                Log.warning("Failed to render media " +
                            "[media=" + media + ", e=" + e + "].");
                Log.logStackTrace(e);
            }
        }
    }

    /**
     * Must be called every frame so that the media can be properly
     * updated.
     */
    public void tick (long tickStamp)
    {
        _tickStamp = tickStamp;
        tickAllMedia(tickStamp);
        dispatchNotifications();
        _tickStamp = 0;
    }

    /**
     * If the manager is paused for some length of time, it should
     * be fast forwarded by the appropriate number of milliseconds.  This
     * allows media to smoothly pick up where they left off rather than
     * abruptly jumping into the future, thinking that some outrageous
     * amount of time passed since their last tick.
     */
    public void fastForward (long timeDelta)
    {
        for (int ii=0, nn=_media.size(); ii < nn; ii++) {
            ((AbstractMedia) _media.get(ii)).fastForward(timeDelta);
        }
    }

    /**
     * Calls {@link AbstractMedia#tick} on all media to give them a chance
     * to move about, change their look, generate dirty regions, and so
     * forth.
     */
    protected void tickAllMedia (long tickStamp)
    {
        // we tick media in reverse order, because some may remove
        // themselves on the tick
        for (int ii=_media.size() - 1; ii >= 0; ii--) {
            ((AbstractMedia) _media.get(ii)).tick(tickStamp);
        }
    }

    /**
     * Inserts the specified media into this manager, return true on
     * success.
     */
    protected boolean insertMedia (AbstractMedia media)
    {
        if (_media.contains(media)) {
            Log.warning("Attempt to insert media more than once " +
                        "[media=" + media + "].");
            return false;
        }

        _media.insertSorted(media, RENDER_ORDER);
        media.init(this);
        if (_tickStamp > 0) {
            // if we're in the middle of ticking, tick the inserted media
            // so that we can paint it when the time comes
            media.tick(_tickStamp);
        }
        return true;
    }

    /**
     * Removes the specified media from this manager, return true on
     * success.
     */
    protected boolean removeMedia (AbstractMedia media)
    {
        if (_media.remove(media)) {
            media.invalidate();
            media.shutdown();
            return true;
        }
        Log.warning("Attempt to remove media that wasn't inserted " +
                    "[media=" + media + "].");
        return false;
    }

    /**
     * Clears all media from the manager. This does not invalidate their
     * vacated bounds; it is assumed that it will be ok.
     */
    protected void clearMedia ()
    {
        for (int ii=_media.size() - 1; ii >= 0; ii--) {
            AbstractMedia media = (AbstractMedia) _media.remove(ii);
            media.shutdown();
        }
    }

    /**
     * Queues the event for dispatching after we've ticked all
     * the media.
     */
    public void queueNotification (ObserverList observers, Object event)
    {
        _notify.add(new Tuple(observers, event));
    }

    /**
     * Dispatches all queued events.
     */
    protected void dispatchNotifications ()
    {
        for (int ii=0, nn=_notify.size(); ii < nn; ii++) {
            Tuple tuple = (Tuple)_notify.remove(0);
            dispatchEvent((ObserverList)tuple.left, tuple.right);
        }
    }

    /**
     * Dispatches the specified event to the specified observers.
     */
    protected abstract void dispatchEvent (
        ObserverList observers, Object event);

    /** The region manager. */
    protected RegionManager _remgr;

    /** List of observers to notify at the end of the tick. */
    protected ArrayList _notify = new ArrayList();

    /** Our render-order sorted list of media. */
    protected SortableArrayList _media = new SortableArrayList();

    /** The tick stamp if the manager is in the midst of a call to {@link
     * #tick}, else <code>0</code>. */
    protected long _tickStamp;

    /** Used to sort media by render order. */
    protected static final Comparator RENDER_ORDER = new Comparator() {
        public int compare (Object o1, Object o2) {
            int result = (((AbstractMedia)o1)._renderOrder -
                          ((AbstractMedia)o2)._renderOrder);
            return (result != 0) ? result : 
                // find some other way to keep them stable relative to
                // each other
                o1.hashCode() - o2.hashCode();
        }
    };
}
