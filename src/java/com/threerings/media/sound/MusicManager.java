//
// $Id: SoundManager.java 3290 2004-12-29 21:56:58Z ray $
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

import java.util.Iterator;
import java.util.LinkedList;

import com.samskivert.util.Config;
import com.samskivert.util.RunQueue;

import com.threerings.util.RandomUtil;

import com.threerings.media.Log;

/**
 * Manages the playing of audio files.
 */
// TODO:
//   - fade music out when stopped?
//   - be able to pause music
public class MusicManager
{
    /**
     * Constructs a music manager.
     *
     * @param smgr The soundManager we work with.
     * @param runQueue the client event run queue.
     *
     */
    public MusicManager (SoundManager smgr, RunQueue runQueue)
    {
        _smgr = smgr;
        _runQueue = runQueue;
    }

    /**
     * Shut the damn thing off.
     */
    public void shutdown ()
    {
        _musicStack.clear();
        stopMusicPlayer();
    }

    /**
     * Returns a string summarizing our volume settings and disabled sound
     * types.
     */
    public String summarizeState ()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("musicVol=").append(_musicVol);
        return buf.append("]").toString();
    }

    /**
     * Sets the volume for music.
     *
     * @param val a volume parameter between 0f and 1f, inclusive.
     */
    public void setMusicVolume (float vol)
    {
        float oldvol = _musicVol;
        _musicVol = Math.max(0f, Math.min(1f, vol));
        if (_musicPlayer != null) {
            _musicPlayer.setVolume(_musicVol);
        }

        if ((oldvol == 0f) && (_musicVol != 0f)) {
            playTopMusic();
        } else if ((oldvol != 0f) && (_musicVol == 0f)) {
            stopMusicPlayer();
        }
    }

    /**
     * Get the music volume.
     */
    public float getMusicVolume ()
    {
        return _musicVol;
    }

    /**
     * Start playing the specified music repeatedly.
     */
    public void pushMusic (String pkgPath, String key)
    {
        pushMusic(pkgPath, key, -1);
    }

    /**
     * Start playing music for the specified number of loops. If no other
     * music is pushed, the specified music will play for the number of loops
     * specified, and will then be popped off the stack.
     */
    public void pushMusic (String pkgPath, String key, int numloops)
    {
        MusicKey mkey = new MusicKey(pkgPath, key, numloops);

        // stop any existing playing music
        if (_musicPlayer != null) {
            _musicPlayer.stop();
            handleMusicStopped();
        }

        // add the new song
        _musicStack.addFirst(mkey);

        // and play it
        playTopMusic();
    }

    /**
     * Remove the specified music from the playlist. If it is currently
     * playing, it will be stopped and the previous song will be started.
     */
    public void removeMusic (String pkgPath, String key)
    {
        MusicKey mkey = new MusicKey(pkgPath, key, -1);

        if (!_musicStack.isEmpty()) {
            MusicKey current = (MusicKey) _musicStack.getFirst();

            // if we're currently playing this song..
            if (mkey.equals(current)) {
                // stop it
                if (_musicPlayer != null) {
                    _musicPlayer.stop();
                }

                // remove it from the stack
                _musicStack.removeFirst();
                // start playing the next..
                playTopMusic();
                return;

            } else {
                // we aren't currently playing this song. Simply remove.
                for (Iterator iter=_musicStack.iterator(); iter.hasNext(); ) {
                    if (key.equals(iter.next())) {
                        iter.remove();
                        return;
                    }
                }

            }
        }

        Log.debug("Sequence stopped that wasn't in the stack anymore " +
            "[key=" + mkey + "].");
    }

    /**
     * Start the specified sequence.
     */
    protected void playTopMusic ()
    {
        if (_musicStack.isEmpty()) {
            return;
        }

        // if the volume is off, we don't actually want to play anything
        // but we want to at least decrement any loopers by one
        // and keep them on the top of the queue
        if (_musicVol == 0f) {
            handleMusicStopped();
            return;
        }

        MusicKey info = (MusicKey) _musicStack.getFirst();

        Config c = _smgr.getConfig(info);
        String[] names = c.getValue(info.key, (String[])null);
        if ((names == null) || (names.length == 0)) {
            Log.warning("No such music [key=" + info + "].");
            _musicStack.removeFirst();
            playTopMusic();
            return;
        }
        String music = names[RandomUtil.getInt(names.length)];

        Class playerClass = getMusicPlayerClass(music);

        // if we don't have a player for this song, play the next song
        if (playerClass == null) {
            _musicStack.removeFirst();
            playTopMusic();
            return;
        }

        // shutdown the old player if we're switching music types
        if (! playerClass.isInstance(_musicPlayer)) {
            if (_musicPlayer != null) {
                _musicPlayer.shutdown();
            }

            // set up the new player
            try {
                _musicPlayer = (MusicPlayer) playerClass.newInstance();
                _musicPlayer.init(_playerListener);

            } catch (Exception e) {
                Log.warning("Unable to instantiate music player [class=" +
                        playerClass + ", e=" + e + "].");

                // scrap it, try again with the next song
                _musicPlayer = null;
                _musicStack.removeFirst();
                playTopMusic();
                return;
            }

            _musicPlayer.setVolume(_musicVol);
        }

        // play!
        String bundle = c.getValue("bundle", (String)null);
        try {
            // TODO: buffer for the music player?
            _musicPlayer.start(_smgr._rmgr.getResource(bundle, music));
        } catch (Exception e) {
            Log.warning("Error playing music, skipping [e=" + e +
                ", bundle=" + bundle + ", music=" + music + "].");
            _musicStack.removeFirst();
            playTopMusic();
            return;
        }
    }

    /**
     * Get the appropriate music player for the specified music file.
     */
    protected static Class getMusicPlayerClass (String path)
    {
        path = path.toLowerCase();

//         if (path.endsWith(".mid") || path.endsWith(".rmf")) {
//             return MidiPlayer.class;

//         } else if (path.endsWith(".mod")) {
//             return ModPlayer.class;

//         } else if (path.endsWith(".mp3")) {
//             return Mp3Player.class;

//         } else if (path.endsWith(".ogg")) {
//             return OggPlayer.class;

//         } else {
            return null;
//         }
    }

    /**
     * Stop whatever song is currently playing and deal with the
     * MusicKey associated with it.
     */
    protected void handleMusicStopped ()
    {
        if (_musicStack.isEmpty()) {
            return;
        }

        // see what was playing
        MusicKey current = (MusicKey) _musicStack.getFirst();

        // see how many times the song was to loop and act accordingly
        switch (current.loops) {
        default:
            current.loops--;
            break;

        case 1:
            // sorry charlie
            _musicStack.removeFirst();
            break;
        }
    }

    /**
     * Stop the current music player.
     */
    protected void stopMusicPlayer ()
    {
        if (_musicPlayer != null) {
            _musicPlayer.stop();
            _musicPlayer.shutdown();
            _musicPlayer = null;
        }
    }

    /**
     * A class that tracks the information about our playing music files.
     */
    protected static class MusicKey extends SoundManager.SoundKey
    {
        /** How many times to loop, or -1 for forever. */
        public int loops;

        public MusicKey (String set, String path, int loops)
        {
            super((byte) -1, set, path);
            this.loops = loops;
        }
    }

    /** The sound manager we work with. */
    protected SoundManager _smgr;

    /** The client event run queue. */
    protected RunQueue _runQueue;

    /** Volume level for music. */
    protected float _musicVol = 1f;

    /** The stack of songs that we're playing. */
    protected LinkedList _musicStack = new LinkedList();

    /** The current music player, if any. */
    protected MusicPlayer _musicPlayer;

    /** Event listener for receiving information about a song ending. */
    protected MusicPlayer.MusicEventListener _playerListener =
        new MusicPlayer.MusicEventListener() {
            public void musicStopped () {
                _runQueue.postRunnable(new Runnable() {
                    public void run() {
                        handleMusicStopped();
                        playTopMusic();
                    }
                });
            }
        };
}
