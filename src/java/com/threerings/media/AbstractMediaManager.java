//
// $Id: AbstractMediaManager.java,v 1.1 2002/10/08 21:03:37 ray Exp $

package com.threerings.media;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

import java.util.ArrayList;
import java.util.Comparator;

import com.samskivert.util.SortableArrayList;
import com.samskivert.util.StringUtil;

/**
 * Manages, ticks, and paints AbstractMedia.
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
     * Provides access to the region manager that the mediamanager is using.
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
        tickAllMedia(tickStamp);
        dispatchNotifications();
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
     * Call {@link AbstractMedia#tick} on all media to give
     * them a chance to move about, change their look, generate dirty
     * regions, and so forth.
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
     * Insert the specified media into this manager, return true on success.
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
        return true;
    }

    /**
     * Remove the specified media from this manager, return true on success.
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
     * Clears all media from the manager. This does not invalidate
     * their vacated bounds, it is assumed that it will be ok.
     */
    protected void clearMedia ()
    {
        for (int ii=_media.size() - 1; ii >= 0; ii--) {
            AbstractMedia media = (AbstractMedia) _media.remove(ii);
            media.shutdown();
        }
    }

    /**
     * Queue the event for dispatching after we've ticked all
     * the media.
     */
    public void queueNotification (ArrayList observers, Object event)
    {
        _notify.add(new Object[] {observers, event});
    }

    /**
     * Dispatch all queued events.
     */
    protected void dispatchNotifications ()
    {
        for (int ii=0, nn=_notify.size(); ii < nn; ii++) {
            Object[] junk = (Object[]) _notify.remove(0);
            ArrayList observers = (ArrayList) junk[0];
            Object event = junk[1];

            dispatchEvent(observers, event);
        }
    }

    /**
     * Dispatch the specified event to the specified observers.
     */
    protected abstract void dispatchEvent (ArrayList observers, Object event);

    /** The Region Manager. */
    protected RegionManager _remgr;

    /** List of observers to notify at the end of the tick. */
    protected ArrayList _notify = new ArrayList();

    /** Our render-order sorted list of media. */
    protected SortableArrayList _media = new SortableArrayList();

    /** Used to sort media by render order. */
    protected static final Comparator RENDER_ORDER = new Comparator() {
        public int compare (Object o1, Object o2) {
            int result = (((AbstractMedia)o1)._renderOrder -
                          ((AbstractMedia)o2)._renderOrder);
            if (result != 0) {
                return result;

            } else {
                // find some other way to keep them stable relative to each
                // other.
                return o1.hashCode() - o2.hashCode();
            }
        }
    };
}
