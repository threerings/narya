//
// $Id: MusicPlayer.java,v 1.1 2002/11/22 04:23:31 ray Exp $

package com.threerings.media;

import com.threerings.resource.ResourceManager;

/**
 * Does something extraordinary.
 */
public abstract class MusicPlayer
{
    /**
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
     */
    public final void init (
        ResourceManager rmgr, MusicEventListener musicListener)
    {
        _rmgr = rmgr;
        _musicListener = musicListener;

        init();
    }

    /**
     * Do your init here.
     */
    public void init ()
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
