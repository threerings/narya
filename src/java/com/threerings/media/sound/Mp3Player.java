//
// $Id: Mp3Player.java,v 1.6 2004/08/27 02:12:40 mdb Exp $
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.threerings.media.Log;

/**
 * Plays mp3 files. Depends on three external jar files that aren't even
 * imported here:
 *     tritonus_share.jar
 *     tritonus_mp3.jar
 *     javalayer.jar
 */
public class Mp3Player extends MusicPlayer
{
    // documentation inherited
    public void init ()
    {
        // TODO: some stuff needs to move here, like setting up the line
        // but we don't yet know the audio format, so I need to figure that
        // out (the format might always be known..).
    }

    // documentation inherited
    public void shutdown ()
    {
    }

    // documentation inherited
    public void start (final InputStream stream)
        throws Exception
    {
        // TODO: some stuff needs to come out of here and into init/shutdown
        // but we'll deal with all that later, d00d.
        _player = new Thread("narya mp3 relay") {
            public void run () {
                AudioInputStream inStream = null;
                try {
                    inStream = AudioSystem.getAudioInputStream(
                        new BufferedInputStream(stream, BUFFER_SIZE));
                } catch (Exception e) {
                    Log.warning("MP3 fuckola. [e=" + e + "].");
                    return;
                }

                AudioFormat sourceFormat = inStream.getFormat();
                AudioFormat.Encoding targetEnc =
                    AudioFormat.Encoding.PCM_SIGNED;

                inStream = AudioSystem.getAudioInputStream(
                    targetEnc, inStream);
                AudioFormat format = inStream.getFormat();

                DataLine.Info info = new DataLine.Info(
                    SourceDataLine.class, format);

                try {
                    _line = (SourceDataLine) AudioSystem.getLine(info);
                    _line.open(format);
                } catch (LineUnavailableException lue) {
                    Log.warning("MP3 line unavailable: " + lue);
                    return;
                }

                _line.start();

                byte[] data = new byte[BUFFER_SIZE];
                int count = 0;
                while (count >= 0) {
                    try {
                        count = inStream.read(data, 0, data.length);
                    } catch (IOException ioe) {
                        Log.warning("Error reading MP3: " + ioe);
                        break;
                    }
                    if (count >= 0) {
                        _line.write(data, 0, count);
                    }
                    if (_player != Thread.currentThread()) {
                        return;
                    }
                }

                _line.drain();
                _line.close();
                _musicListener.musicStopped();
            }
        };

        _player.setDaemon(true);
        //_player.setPriority(_player.getPriority() + 1);
        _player.start();
    }

    // documentation inherited
    public void stop ()
    {
        _player = null;
    }

    // documentation inherited
    public void setVolume (float volume)
    {
        // TODO : line won't be null when we initialize it in the right place
        if (_line != null) {
            SoundManager.adjustVolume(_line, volume);
        }
    }

    /** The thread that transfers data to the line. */
    protected Thread _player;

    /** The line that we play through. */
    protected SourceDataLine _line;

    /** The size of our buffer. */
    protected static final int BUFFER_SIZE = 8192;
}
