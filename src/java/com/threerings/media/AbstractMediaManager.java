//
// $Id: AbstractMediaManager.java,v 1.9 2003/04/30 00:43:54 mdb Exp $

package com.threerings.media;

import java.awt.Graphics2D;
import java.awt.Shape;

import java.util.ArrayList;
import java.util.Comparator;

import com.samskivert.util.ObserverList.ObserverOp;
import com.samskivert.util.ObserverList;
import com.samskivert.util.SortableArrayList;
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
        // now that we're rendering, we can safely clear this; see tick()
        _tickStamp = 0;

        for (int ii = 0, nn = _media.size(); ii < nn; ii++) {
            AbstractMedia media = (AbstractMedia)_media.get(ii);
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
        // we clear our tick stamp when we've been painted, this lets us
        // handle situations when yet more media is slipped in between our
        // being ticked and our being painted
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
        if (_tickStamp > 0) {
            Log.warning("Egads! Asked to fastForward() during a tick.");
            Thread.dumpStack();
        }

        for (int ii=0, nn=_media.size(); ii < nn; ii++) {
            ((AbstractMedia) _media.get(ii)).fastForward(timeDelta);
        }
    }

    /**
     * Returns true if the specified media is being managed by this media
     * manager.
     */
    public boolean isManaged (AbstractMedia media)
    {
        return _media.contains(media);
    }

    /**
     * Calls {@link AbstractMedia#tick} on all media to give them a chance
     * to move about, change their look, generate dirty regions, and so
     * forth.
     */
    protected void tickAllMedia (long tickStamp)
    {
        // we use _tickpos so that it can be adjusted if media is added or
        // removed during the tick dispatch
        for (_tickpos = 0; _tickpos < _media.size(); _tickpos++) {
            ((AbstractMedia) _media.get(_tickpos)).tick(tickStamp);
        }
        _tickpos = -1;
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

        media.init(this);
        int ipos = _media.insertSorted(media, RENDER_ORDER);

        // if we've started our tick but have not yet painted our media,
        // we need to take care that this newly added media will be ticked
        // before our upcoming render
        if (_tickStamp > 0L) {
            if (_tickpos == -1) {
                // if we're done with our own call to tick(), we
                // definitely need to tick this new media
                media.tick(_tickStamp);
            } else if (ipos <= _tickpos) {
                // otherwise, we're in the middle of our call to tick()
                // and we only need to tick this guy if he's being
                // inserted before our current tick position (if he's
                // inserted after our current position, we'll get to him
                // as part of this tick iteration)
                _tickpos++;
                media.tick(_tickStamp);
            }
        }

        return true;
    }

    /**
     * Removes the specified media from this manager, return true on
     * success.
     */
    protected boolean removeMedia (AbstractMedia media)
    {
        int mpos = _media.indexOf(media);
        if (mpos != -1) {
            _media.remove(mpos);
            media.invalidate();
            media.shutdown();
            // if we're in the middle of ticking, we need to adjust the
            // _tickpos if necessary
            if (mpos <= _tickpos) {
                _tickpos--;
            }
            return true;
        }
        Log.warning("Attempt to remove media that wasn't inserted " +
                    "[media=" + media + "].");
        return false;
    }

    /**
     * Clears all media from the manager and calls {@link
     * AbstractMedia#shutdown} on each. This does not invalidate the
     * media's vacated bounds; it is assumed that it will be ok.
     */
    protected void clearMedia ()
    {
        if (_tickStamp > 0) {
            Log.warning("Egads! Requested to clearMedia() during a tick.");
            Thread.dumpStack();
        }

        for (int ii=_media.size() - 1; ii >= 0; ii--) {
            AbstractMedia media = (AbstractMedia) _media.remove(ii);
            media.shutdown();
        }
    }

    /**
     * Queues the notification for dispatching after we've ticked all the
     * media.
     */
    public void queueNotification (ObserverList observers, ObserverOp event)
    {
        _notify.add(new Tuple(observers, event));
    }

    /**
     * Dispatches all queued media notifications.
     */
    protected void dispatchNotifications ()
    {
        for (int ii = 0, nn = _notify.size(); ii < nn; ii++) {
            Tuple tuple = (Tuple)_notify.get(ii);
            ((ObserverList)tuple.left).apply((ObserverOp)tuple.right);
        }
        _notify.clear();
    }

    /** The region manager. */
    protected RegionManager _remgr;

    /** List of observers to notify at the end of the tick. */
    protected ArrayList _notify = new ArrayList();

    /** Our render-order sorted list of media. */
    protected SortableArrayList _media = new SortableArrayList();

    /** The position in our media list that we're ticking in the middle of
     * a call to {@link #tick} otherwise -1. */
    protected int _tickpos = -1;

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
