//
// $Id: MusicPlayer.java,v 1.5 2004/08/27 02:12:40 mdb Exp $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.media.sound;

import java.io.InputStream;

/**
 * Abstract music player.
 */
public abstract class MusicPlayer
{
    /**
     * A watcher interested in music events.
     */
    public interface MusicEventListener
    {
        /**
         * A callback that all players should use when the song they are
         * playing has finished playing (completely).
         */
        public void musicStopped();
    }

    /**
     * Initialize the music player.
     */
    public final void init (MusicEventListener musicListener)
        throws Exception
    {
        _musicListener = musicListener;

        init();
    }

    /**
     * Do your init here.
     */
    public void init ()
        throws Exception
    {
    }

    /**
     * Shutdown and free all resources.
     */
    public void shutdown ()
    {
    }

    /**
     * Start playing song data from the specified stream.
     */
    public abstract void start (InputStream stream)
        throws Exception;

    /**
     * Stop playing the specified song.
     */
    public abstract void stop ();

    /**
     * Set the volume.
     *
     * @param volume 0f - 1f, inclusive.
     */
    public abstract void setVolume (float volume);

    /** Tell this guy about it when a song stops. */
    protected MusicEventListener _musicListener;
}
