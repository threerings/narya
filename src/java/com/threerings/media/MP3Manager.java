//
// $Id: MP3Manager.java,v 1.2 2002/11/20 04:03:09 ray Exp $

package com.threerings.media;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine.Info;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import org.apache.commons.io.StreamUtils;

import com.threerings.resource.ResourceManager;

/**
 * Does something extraordinary.
 */
public class MP3Manager
{
    public MP3Manager (ResourceManager rmgr, SoundManager smgr)
    {
        _rmgr = rmgr;
        _smgr = smgr;
    }

    public void stop ()
    {
        _player = null;
    }

    public void start (final String set, final String path)
    {
        _player = new Thread("narya mp3 relay") {
            public void run () {
                AudioInputStream inStream = null;
                try {
                    inStream = AudioSystem.getAudioInputStream(
//                       new ByteArrayInputStream(StreamUtils.streamAsBytes(
//                               _rmgr.getResource(path), 8192)));
                        // TODO: use the fucking resource sets
                        new BufferedInputStream(
                            _rmgr.getResource(set, path), 4096));
                } catch (Exception e) {
                    Log.warning("MP3 fuckola [path=" + path +
                        ", e=" + e + "].");
                    return;
                }

                AudioFormat sourceFormat = inStream.getFormat();
                AudioFormat.Encoding targetEnc =
                    AudioFormat.Encoding.PCM_SIGNED;

                inStream = AudioSystem.getAudioInputStream(
                    targetEnc, inStream);
                AudioFormat format = inStream.getFormat();

                SourceDataLine line = null;
                DataLine.Info info = new DataLine.Info(
                    SourceDataLine.class, format);

                try {
                    line = (SourceDataLine) AudioSystem.getLine(info);
                    line.open(format);
                } catch (LineUnavailableException lue) {
                    Log.warning("MP3 line unavailable: " + lue);
                    return;
                }

                line.start();

                byte[] data = new byte[BUFFER_SIZE];
                int count = 0;
                while (_player == Thread.currentThread()) {
                    try {
                        count = inStream.read(data, 0, data.length);
                    } catch (IOException ioe) {
                        Log.warning("Error reading MP3: " + ioe);
                        break;
                    }
                    if (count >= 0) {
                        line.write(data, 0, count);
                    } else {
                        break;
                    }
                }

                line.close();
                _smgr.songStopEvent();
            }
        };

        _player.setDaemon(true);
        _player.setPriority(_player.getPriority() + 1);
        _player.start();
    }

    protected Thread _player;

    protected ResourceManager _rmgr;
    protected SoundManager _smgr;

    protected static final int BUFFER_SIZE = 128000;
}
