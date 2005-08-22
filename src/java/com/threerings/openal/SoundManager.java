//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.openal;

import java.io.IOException;
import java.util.HashMap;
import java.util.WeakHashMap;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import com.samskivert.util.LRUHashMap;
import com.samskivert.util.Queue;
import com.samskivert.util.RunQueue;

/**
 * An interface to the OpenAL library that provides a number of additional
 * services:
 *
 * <ul>
 * <li> an object oriented interface to the OpenAL system
 * <li> a mechanism for loading a group of sounds and freeing their
 * resources all at once
 * <li> a mechanism for loading sounds in a background thread and
 * preloading sounds that are likely to be needed soon
 * </ul>
 *
 * <p><em>Note:</em> the sound manager is not thread safe (other than
 * during its interactions with its internal background loading
 * thread). It assumes that all sound loading and play requests will be
 * made from a single thread.
 */
public class SoundManager
{
    /**
     * Creates, initializes and returns the singleton sound manager
     * instance.
     *
     * @param rqueue a queue that the sound manager can use to post short
     * runnables that must be executed on the same thread from which all
     * other sound methods will be called.
     */
    public static SoundManager createSoundManager (RunQueue rqueue)
    {
        if (_soundmgr != null) {
            throw new IllegalStateException(
                "A sound manager has already been created.");
        }
        _soundmgr = new SoundManager(rqueue);
        return _soundmgr;
    }

    /**
     * Returns true if we were able to initialize the sound system.
     */
    public boolean isInitialized ()
    {
        return (_toLoad != null);
    }

    /**
     * Configures the size of our sound cache. If this value is larger
     * than memory available to the underlying sound system, it will be
     * reduced when OpenAL first tells us we're out of memory.
     */
    public void setCacheSize (int bytes)
    {
        _clips.setMaxSize(bytes);
    }

    /**
     * Creates an object that can be used to manage and play a group of
     * sounds. <em>Note:</em> the sound group <em>must</em> be disposed
     * when it is no longer needed via a call to {@link
     * SoundGroup#dispose}.
     *
     * @param provider indicates from where the sound group will load its
     * sounds.
     * @param sources indicates the maximum number of simultaneous sounds
     * that can play in this group.
     */
    public SoundGroup createGroup (ClipProvider provider, int sources)
    {
        return new SoundGroup(this, provider, sources);
    }

    /**
     * Creates a sound manager and initializes the OpenAL sound subsystem.
     */
    protected SoundManager (RunQueue rqueue)
    {
        _rqueue = rqueue;

        // initialize the OpenAL sound system
        try {
            AL.create("", 44100, 15, false);
        } catch (Exception e) {
            Log.warning("Failed to initialize sound system.");
            Log.logStackTrace(e);
            // don't start the background loading thread
            return;
        }

        int errno = AL10.alGetError();
        if (errno != AL10.AL_NO_ERROR) {
            Log.warning("Failed to initialize sound system " +
                        "[errno=" + errno + "].");
            // don't start the background loading thread
            return;
        }

        // configure our LRU map with a removal observer
        _clips.setRemovalObserver(new LRUHashMap.RemovalObserver() {
            public void removedFromMap (LRUHashMap map, Object item) {
                ((ClipBuffer)item).dispose();
            }
        });

        // create our loading queue
        _toLoad = new Queue();

        // start up the background loader thread
        _loader.setDaemon(true);
        _loader.start();
    }

    /**
     * Creates a clip buffer for the sound clip loaded via the specified
     * provider with the specified path. The clip buffer may come from teh
     * cache, and it will immediately be queued for loading if it is not
     * already loaded.
     */
    protected ClipBuffer getClip (ClipProvider provider, String path)
    {
        Comparable ckey = ClipBuffer.makeKey(provider, path);
        ClipBuffer buffer = (ClipBuffer)_clips.get(ckey);
        if (buffer == null) {
            // check to see if this clip is currently loading
            buffer = (ClipBuffer)_loading.get(ckey);
            if (buffer == null) {
                buffer = new ClipBuffer(this, provider, path);
                _loading.put(ckey, buffer);
            }
        }
        buffer.resolve(null);
        return buffer;
    }

    /**
     * Queues the supplied clip buffer up for resolution. The {@link Clip}
     * will be loaded into memory and then bound into OpenAL on the
     * background thread.
     */
    protected void queueClipLoad (ClipBuffer buffer)
    {
        if (_toLoad != null) {
            _toLoad.append(buffer);
        }
    }

    /**
     * Queues the supplied clip buffer up using our {@link RunQueue} to
     * notify its observers that it failed to load.
     */
    protected void queueClipFailure (final ClipBuffer buffer)
    {
        _rqueue.postRunnable(new Runnable() {
            public void run () {
                _loading.remove(buffer.getKey());
                buffer.failed();
            }
        });
    }

    /** The thread that loads up sound clips in the background. */
    protected Thread _loader = new Thread("SoundManager.Loader") {
        public void run () {
            while (true) {
                final ClipBuffer buffer = (ClipBuffer)_toLoad.get();
                try {
                    final Clip clip = buffer.load();
                    _rqueue.postRunnable(new Runnable() {
                        public void run () {
                            Comparable ckey = buffer.getKey();
                            _loading.remove(ckey);
                            if (buffer.bind(clip)) {
                                _clips.put(ckey, buffer);
                            } else {
                                // TODO: shrink the cache size if the bind
                                // failed due to OUT_OF_MEMORY
                            }
                        }
                    });

                } catch (Throwable t) {
                    Log.warning("Failed to load clip " +
                                "[key=" + buffer.getKey() + "].");
                    Log.logStackTrace(t);

                    // let the clip and its observers know that we are a
                    // miserable failure
                    queueClipFailure(buffer);
                }
            }
        }
    };

    /** Used to get back from the background thread to our "main" thread. */
    protected RunQueue _rqueue;

    /** Contains a mapping of all currently-loading clips. */
    protected HashMap _loading = new HashMap();

    /** Contains a mapping of all loaded clips. */
    protected LRUHashMap _clips = new LRUHashMap(DEFAULT_CACHE_SIZE, _sizer);

    /** Contains a queue of clip buffers waiting to be loaded. */
    protected Queue _toLoad;

    /** The one and only sound manager, here for an exclusive performance
     * by special request. Available for all your sound playing needs. */
    protected static SoundManager _soundmgr;

    /** Used to compute the in-memory size of sound samples. */
    protected static LRUHashMap.ItemSizer _sizer = new LRUHashMap.ItemSizer() {
        public int computeSize (Object item) {
            return ((ClipBuffer)item).getSize();
        }
    };

    /** Default to a cache size of one megabyte. */
    protected static final int DEFAULT_CACHE_SIZE = 1024 * 1024;
}
