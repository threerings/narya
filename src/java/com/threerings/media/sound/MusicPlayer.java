//
// $Id: MusicPlayer.java,v 1.2 2002/11/22 19:21:12 ray Exp $

package com.threerings.media;

import com.threerings.resource.ResourceManager;

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
    public final void init (
        ResourceManager rmgr, MusicEventListener musicListener)
        throws Exception
    {
        _rmgr = rmgr;
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
     * Start playing the specified song.
     */
    public abstract void start (String set, String path)
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

    /** The place we load data from. */
    protected ResourceManager _rmgr;

    /** Tell this guy about it when a song stops. */
    protected MusicEventListener _musicListener;
}
