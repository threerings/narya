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

import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

/**
 * Manages a group of sounds, binding them to OpenAL sources as they are
 * played and freeing up those sources for use by other sounds when the
 * sounds are finished.
 */
public class SoundGroup
{
    /**
     * Queues up the specified sound clip for pre-loading into the cache.
     */
    public void preloadClip (String path)
    {
        _manager.getClip(_provider, path);
    }

    /**
     * Obtains an "instance" of the specified sound which can be
     * positioned, played, looped and otherwise used to make noise.
     */
    public Sound getSound (String path)
    {
        ClipBuffer buffer = null;
        if (_manager.isInitialized()) {
            buffer = _manager.getClip(_provider, path);
        }
        return (buffer == null) ? new BlankSound() : new Sound(this, buffer);
    }

    /**
     * Disposes this sound group, freeing up the OpenAL sources with which
     * it is associated. All sounds obtained from this group will no
     * longer be usable and should be discarded.
     */
    public void dispose ()
    {
        if (_sourceIds != null) {
            AL10.alDeleteSources(_sourceIds);
            _sourceIds = null;
        }
    }

    protected SoundGroup (
        SoundManager manager, ClipProvider provider, int sources)
    {
        _manager = manager;
        _provider = provider;

        // if we were unable to initialize the sound system at all, just
        // stop here and we'll behave as if we have no available sources
        if (!_manager.isInitialized()) {
            return;
        }

        // create our sources
        _sourceIds = BufferUtils.createIntBuffer(sources);
        AL10.alGenSources(_sourceIds);
        int errno = AL10.alGetError();
        if (errno != AL10.AL_NO_ERROR) {
            Log.warning("Failed to create sources [cprov=" + provider +
                        ", sources=" + sources + ", errno=" + errno + "].");
            _sourceIds = null;
            // we'll have no sources which means all requests to play
            // sounds will silently fail as if all sources were in use

        } else {
            for (int ii = 0; ii < sources; ii++) {
                Source source = new Source();
                source.sourceId = _sourceIds.get(ii);
                _sources.add(source);
            }
        }
    }

    /**
     * Called by a {@link Sound} when it wants to obtain a source on which
     * to play its clip.
     */
    protected int acquireSource (Sound acquirer)
    {
        // start at the beginning of the list looking for an available
        // source
        for (int ii = 0, ll = _sources.size(); ii < ll; ii++) {
            Source source = _sources.get(ii);
            if (source.holder == null || source.holder.reclaim()) {
                // note this source's new holder
                source.holder = acquirer;
                // move this source to the end of the list
                _sources.remove(ii);
                _sources.add(source);
                return source.sourceId;
            }
        }
        return -1;
    }

    /** Used to track which sources are in use. */
    protected static class Source
    {
        public int sourceId;
        public Sound holder;
    }

    protected SoundManager _manager;
    protected ClipProvider _provider;

    protected IntBuffer _sourceIds;
    protected ArrayList<Source> _sources = new ArrayList<Source>();
}
