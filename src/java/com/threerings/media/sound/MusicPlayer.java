//
// $Id: MusicPlayer.java,v 1.4 2003/01/08 04:09:02 mdb Exp $

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
