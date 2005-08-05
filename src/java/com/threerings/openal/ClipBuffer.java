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
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import com.samskivert.util.LRUHashMap;
import com.samskivert.util.ObserverList;

/**
 * Represents a sound that has been loaded into the OpenAL system.
 */
public class ClipBuffer
    implements LRUHashMap.LRUItem
{
    /** Used to notify parties interested in when a clip is loaded. */
    public static interface Observer
    {
        /** Called when a clip has completed loading and is ready to be
         * played. */
        public void clipLoaded (ClipBuffer buffer);

        /** Called when a clip has failed to prepare itself for one reason
         * or other. */
        public void clipFailed (ClipBuffer buffer);
    }

    /**
     * Create a key that uniquely identifies this combination of clip
     * provider and path.
     */
    public static Comparable makeKey (ClipProvider provider, String path)
    {
        // we'll just use a string, amazing!
        return provider + path;
    }

    /**
     * Creates a new clip buffer with the specified path that will obtain
     * its clip data from the specified source. The clip will
     * automatically queue itself up to be loaded into memory.
     */
    public ClipBuffer (SoundManager manager, ClipProvider provider, String path)
    {
        _manager = manager;
        _provider = provider;
        _path = path;
    }

    /**
     * Returns the unique key for this clip buffer.
     */
    public Comparable getKey ()
    {
        return makeKey(_provider, _path);
    }

    /**
     * Returns the provider used to load this clip.
     */
    public ClipProvider getClipProvider ()
    {
        return _provider;
    }

    /**
     * Returns the path that identifies this sound clip.
     */
    public String getPath ()
    {
        return _path;
    }

    /**
     * Returns true if this buffer is loaded and ready to go.
     */
    public boolean isPlayable ()
    {
        return (_state == LOADED);
    }

    /**
     * Returns the identifier for this clip's buffer or -1 if it is not
     * loaded.
     */
    public int getBufferId ()
    {
        return (_bufferId == null) ? -1 : _bufferId.get(0);
    }

    /**
     * Returns the size (in bytes) of this clip as reported by OpenAL.
     * This value will not be valid until the clip is bound.
     */
    public int getSize ()
    {
        return _size;
    }

    /**
     * Instructs this buffer to resolve its underlying clip and be ready
     * to be played ASAP.
     */
    public void resolve (Observer observer)
    {
        // if we're already loaded, this is easy
        if (_state == LOADED) {
            if (observer != null) {
                observer.clipLoaded(this);
            }
            return;
        }

        // queue up the observer
        if (observer != null) {
            _observers.add(observer);
        }

        // if we're already loading, we can stop here
        if (_state == LOADING) {
            return;
        }

        // create our OpenAL buffer and then queue ourselves up to have
        // our clip data loaded
        _bufferId = BufferUtils.createIntBuffer(1);
        AL10.alGenBuffers(_bufferId);
        int errno = AL10.alGetError();
        if (errno != AL10.AL_NO_ERROR) {
            Log.warning("Failed to create buffer [key=" + getKey() +
                        ", errno=" + errno + "].");
            _bufferId = null;
            // queue up a failure notification so that we properly return
            // from this method and our sound has a chance to register
            // itself as an observer before we jump up and declare failure
            _manager.queueClipFailure(this);

        } else {
            _state = LOADING;
            _manager.queueClipLoad(this);
        }
    }

    // documentation inherited from interface LRUHashMap.LRUItem
    public void removedFromMap (LRUHashMap map)
    {
        if (_bufferId != null) {
            // we've been given the boot, free up our buffer
            AL10.alDeleteBuffers(_bufferId);
            _bufferId = null;
            _state = UNLOADED;
        }
    }

    /**
     * This method is called by the background sound loading thread and
     * actually loads the sound data from wherever it cometh.
     */
    protected Clip load ()
        throws IOException
    {
        return _provider.loadClip(_path);
    }

    /**
     * This method is called back on the main thread and instructs this
     * buffer to bind the clip data to this buffer's OpenAL buffer.
     *
     * @return true if the binding succeeded, false if we were unable to
     * load the sound data into OpenAL.
     */
    protected boolean bind (Clip clip)
    {
        AL10.alBufferData(
            _bufferId.get(0), clip.format, clip.data, clip.frequency);
        int errno = AL10.alGetError();
        if (errno != AL10.AL_NO_ERROR) {
            Log.warning("Failed to bind clip [key=" + getKey() +
                        ", errno=" + errno + "].");
            failed();
            return false;
        }

        _state = LOADED;
        _size = AL10.alGetBufferi(_bufferId.get(0), AL10.AL_SIZE);
        _observers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((Observer)observer).clipLoaded(ClipBuffer.this);
                return true;
            }
        });
        _observers.clear();
        return true;
    }

    /**
     * Called when we fail in some part of the process in resolving our
     * clip data. Notifies our observers and resets the clip to the
     * UNLOADED state.
     */
    protected void failed ()
    {
        if (_bufferId != null) {
            AL10.alDeleteBuffers(_bufferId);
            _bufferId = null;
        }
        _state = UNLOADED;

        _observers.apply(new ObserverList.ObserverOp() {
            public boolean apply (Object observer) {
                ((Observer)observer).clipFailed(ClipBuffer.this);
                return true;
            }
        });
        _observers.clear();
    }

    protected SoundManager _manager;
    protected ClipProvider _provider;
    protected String _path;
    protected int _state;
    protected IntBuffer _bufferId;
    protected int _size;
    protected ObserverList _observers =
        new ObserverList(ObserverList.FAST_UNSAFE_NOTIFY);

    protected static final int UNLOADED = 0;
    protected static final int LOADING = 1;
    protected static final int LOADED = 2;
}
