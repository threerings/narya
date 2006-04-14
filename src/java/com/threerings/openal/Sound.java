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

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

/**
 * Represents an instance of a sound clip which can be positioned in 3D
 * space, gain and pitch adjusted and played or looped.
 */
public class Sound
{
    /** Used to await notification of the starting of a sound which may be
     * delayed in loading. */
    public interface StartObserver
    {
        /** Called when the specified sound has started playing. */
        public void soundStarted (Sound sound);
    }

    /**
     * Returns the buffer of audio data associated with this sound.
     */
    public ClipBuffer getBuffer ()
    {
        return _buffer;
    }

    /**
     * Configures the location of this sound in 3D space. This will not
     * affect an already playing sound but will take effect the next time
     * it is played.
     */
    public void setLocation (float x, float y, float z)
    {
        if (_position == null) {
            _position = BufferUtils.createFloatBuffer(3);
        }
        _position.put(x).put(y).put(z);
        _position.flip();
    }

    /**
     * Configures the velocity of this sound in 3D space, in delta
     * location per second (see {@link #setLocation}). This will not
     * affect an already playing sound but will take effect the next time
     * it is played.
     */
    public void setVelocity (float dx, float dy, float dz)
    {
        if (_velocity == null) {
            _velocity = BufferUtils.createFloatBuffer(3);
        }
        _velocity.put(dx).put(dy).put(dz);
        _velocity.flip();
    }

    /**
     * Configures the sounds's pitch. This value can range from 0.5 to
     * 2.0. This will not affect an already playing sound but will take
     * effect the next time it is played.
     */
    public void setPitch (float pitch)
    {
        _pitch = pitch;
    }

    /**
     * Configures the sound's gain (volume). This value can range from 0.0
     * to 1.0 with 1.0 meaning no attenuation, each division by two
     * corresponding to a -6db attentuation and each multiplication by two
     * corresponding to +6db amplification. This will not affect an
     * already playing sound but will take effect the next time it is
     * played.
     */
    public void setGain (float gain)
    {
        _gain = gain;
    }

    /**
     * Plays this sound from the beginning. While the sound is playing, an
     * audio channel will be locked and then freed when the sound
     * completes.
     *
     * @param allowDefer if false, the sound will be played immediately or
     * not at all. If true, the sound will be queued up for loading if it
     * is currently flushed from the cache and played once loaded.
     *
     * @return true if the sound could be played and was started (or
     * queued up to be loaded and played ASAP if it was specified as
     * deferrable) or false if the sound could not be played either
     * because it was not ready and deferral was not allowed or because
     * too many other sounds were playing concurrently.
     */
    public boolean play (boolean allowDefer)
    {
        return play(allowDefer, false, null);
    }

    /**
     * Loops this sound, starting from the beginning of the audio data. It
     * will continue to loop until {@link #pause}d or {@link #stop}ped.
     * While the sound is playing an audio channel will be locked.
     *
     * @return true if a channel could be obtained to play the sound (and
     * the sound was thus started) or false if no channels were available.
     */
    public boolean loop (boolean allowDefer)
    {
        return play(allowDefer, true, null);
    }

    /**
     * Plays this sound from the beginning, notifying the supplied observer
     * when the audio starts.
     *
     * @param loop whether or not to loop the sampe until {@link #stop}ped.
     */
    public void play (StartObserver obs, boolean loop)
    {
        play(true, loop, obs);
    }

    /**
     * Pauses this sound. A subsequent call to {@link #play} will resume
     * the sound from the precise position that it left off. While the
     * sound is paused, its audio channel will remain locked.
     */
    public void pause ()
    {
        if (_sourceId != -1) {
            AL10.alSourcePause(_sourceId);
        }
    }

    /**
     * Stops this sound and rewinds to its beginning. The audio channel
     * being used to play the sound will be released.
     */
    public void stop ()
    {
        if (_sourceId != -1) {
            AL10.alSourceStop(_sourceId);
        }
    }

    protected Sound (SoundGroup group, ClipBuffer buffer)
    {
        _group = group;
        _buffer = buffer;
    }

    protected boolean play (
        boolean allowDefer, final boolean loop, final StartObserver obs)
    {
        // if we're not ready to go...
        if (!_buffer.isPlayable()) {
            if (allowDefer) {
                // resolve the buffer and instruct it to play once it is
                // resolved
                _buffer.resolve(new ClipBuffer.Observer() {
                    public void clipLoaded (ClipBuffer buffer) {
                        play(false, loop, obs);
                    }
                    public void clipFailed (ClipBuffer buffer) {
                        // well, let's pretend like the sound started so that
                        // the observer isn't left hanging
                        if (obs != null) {
                            obs.soundStarted(Sound.this);
                        }
                    }
                });
                return true;
            } else {
                // sorry charlie...
                return false;
            }
        }

        // let the observer know that (as far as they're concerned), we're
        // started
        if (obs != null) {
            obs.soundStarted(this);
        }

        // if we do not already have a source, obtain one
        if (_sourceId == -1) {
            _sourceId = _group.acquireSource(this);
            if (_sourceId == -1) {
                return false;
            }

            // bind our clip buffer to the source
            AL10.alSourcei(_sourceId, AL10.AL_BUFFER, _buffer.getBufferId());
        }

        // configure the source with our ephemera
        AL10.alSourcef(_sourceId, AL10.AL_PITCH, _pitch);
        AL10.alSourcef(_sourceId, AL10.AL_GAIN, _gain);
        if (_position != null) {
            AL10.alSource (_sourceId, AL10.AL_POSITION, _position);
        }
        if (_velocity != null) {
            AL10.alSource (_sourceId, AL10.AL_VELOCITY, _velocity);
        }

        // configure whether or not we should loop
        AL10.alSourcei(_sourceId, AL10.AL_LOOPING,
                       loop ? AL10.AL_TRUE : AL10.AL_FALSE);

        // and start that damned thing up!
        AL10.alSourcePlay(_sourceId);

        return true;
    }

    /**
     * Called by the {@link SoundGroup} when it wants to reclaim our
     * source.
     *
     * @return false if we have no source to reclaim or if we're still busy
     * playing our sound, true if we gave up our source.
     */
    protected boolean reclaim ()
    {
        if (_sourceId != -1 &&
            AL10.alGetSourcei(_sourceId, AL10.AL_SOURCE_STATE) ==
            AL10.AL_STOPPED) {
            _sourceId = -1;
            return true;
        }
        return false;
    }

    /** The sound group with which we are associated. */
    protected SoundGroup _group;

    /** The OpenAL buffer from which we get our sound data. */
    protected ClipBuffer _buffer;

    /** The source via which we are playing our sound currently. */
    protected int _sourceId = -1;

    /** The pitch adjustment. */
    protected float _pitch = 1;

    /** The gain adjustment. */
    protected float _gain = 1;

    /** The starting position in 3D space. */
    protected FloatBuffer _position;

    /** The velocity vector. */
    protected FloatBuffer _velocity;
}
