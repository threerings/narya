//
// $Id: PathSequence.java,v 1.2 2003/05/20 04:09:13 mdb Exp $

package com.threerings.media.util;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import java.util.ArrayList;
import java.util.List;

import com.threerings.media.Log;

/**
 * Used to create a path that is a sequence of several other paths.
 */
public class PathSequence
    implements Path
{
    /**
     * Conveniently construct a path sequence with the two specified
     * paths.
     */
    public PathSequence (Path first, Path second)
    {
        this(new ArrayList());
        _paths.add(first);
        _paths.add(second);
    }

    /**
     * Construct a path sequence with the list of paths.
     * Note: Paths may be added to the end of the list while
     * the pathable is still traversing earlier paths!
     */
    public PathSequence (List paths)
    {
        _paths = paths;
    }

    // documentation inherited from interface Path
    public void init (Pathable pable, long tickStamp)
    {
        _pable = pable;
        _pableRep = new DelegatingPathable(_pable) {
            public void pathCompleted (long timeStamp) {
                long initStamp;
                // if we just finished a timed path, we can figure out how
                // long ago it really finished and init the next path at
                // that time in the past.
                if (_curPath instanceof TimedPath) {
                    initStamp = _lastInit + ((TimedPath) _curPath)._duration;
                } else {
                    // we don't know
                    initStamp = timeStamp;
                }
                initNextPath(initStamp, timeStamp);
            }
        };
        initNextPath(tickStamp, tickStamp);
    }

    // documentation inherited from interface Path
    public boolean tick (Pathable pable, long tickStamp)
    {
        if (pable != _pable) {
            Log.warning("PathSequence ticked with different path than " +
                "it was inited with.");
        }
        return _curPath.tick(_pableRep, tickStamp);
    }

    // documentation inherited from interface Path
    public void fastForward (long timeDelta)
    {
        _lastInit += timeDelta;
        _curPath.fastForward(timeDelta);
    }

    // documentation inherited from interface Path
    public void paint (Graphics2D gfx)
    {
        // for now..
        _curPath.paint(gfx);
    }

    // documentation inherited from interface Path
    public void wasRemoved (Pathable pable)
    {
        _curPath.wasRemoved(_pableRep);
    }

    /**
     * Initialize and start the next path in the sequence.
     */
    protected void initNextPath (long initStamp, long tickStamp)
    {
        if (_paths.size() == 0) {
            _pable.pathCompleted(tickStamp);

        } else {
            _curPath = (Path) _paths.remove(0);
            _lastInit = initStamp;

            _curPath.init(_pableRep, initStamp);
            _curPath.tick(_pableRep, tickStamp);
        }
    }

    /** The list of paths. */
    protected List _paths;

    /** The timestamp at which we last inited a path. */
    protected long _lastInit;

    /** The current path we're pathing. */
    protected Path _curPath;

    /** The pathable we're duping bigtime. */
    protected Pathable _pable;

    /** A fake pathable that we pass to the subpaths. */
    protected Pathable _pableRep;
}
