//
// $Id: OggPlayer.java,v 1.1 2003/02/12 01:44:44 ray Exp $

package com.threerings.media.sound;

import java.io.InputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jcraft.jorbis.*;
import com.jcraft.jogg.*;

/**
 * Plays Ogg Vorbis streams.
 *
 * Hacked together from NASTY code from JOrbis.
 */
// TODO- this would need to be greatly cleaned up if we were serious about
// using it.
public class OggPlayer extends MusicPlayer
{
    // documentation inherited
    public void start (final InputStream stream)
    {
        _player = new Thread("narya ogg player") {
            public void run () {
                playStream(stream);
            }
        };
        _player.setDaemon(true);
        _player.start();
    }

    // documentation inherited
    public void stop ()
    {
        _player = null;
    }

    static final int BUFSIZE=4096*2;
    static int convsize=BUFSIZE*2;
    static byte[] convbuffer=new byte[convsize]; 

    SyncState oy;
    StreamState os;
    Page og;
    Packet op;
    Info vi;
    Comment vc;
    DspState vd;
    Block vb;

    byte[] buffer=null;
    int bytes=0;

    int format;
    int rate=0;
    int channels=0;
    SourceDataLine outputLine=null;

    int frameSizeInBytes;
    int bufferLengthInBytes;

    void init_jorbis(){
        oy=new SyncState();
        os=new StreamState();
        og=new Page();
        op=new Packet();

        vi=new Info();
        vc=new Comment();
        vd=new DspState();
        vb=new Block(vd);

        buffer=null;
        bytes=0;

        oy.init();
    }

  SourceDataLine getOutputLine(int channels, int rate){
    if(outputLine!=null || this.rate!=rate || this.channels!=channels){
      if(outputLine!=null){
        outputLine.drain();
        outputLine.stop();
        outputLine.close();
      }
      init_audio(channels, rate);
      outputLine.start();
    }
    return outputLine;
  }

  void init_audio(int channels, int rate){
    try {
      //ClassLoader originalClassLoader=null;
      //try{
      //  originalClassLoader=Thread.currentThread().getContextClassLoader();
      //  Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
      //}
      //catch(Exception ee){
      //  System.out.println(ee);
      //}
      AudioFormat audioFormat = 
	new AudioFormat((float)rate, 
			16,
			channels,
			true,  // PCM_Signed
			false  // littleEndian
			);
      DataLine.Info info = 
	new DataLine.Info(SourceDataLine.class,
			  audioFormat, 
			  AudioSystem.NOT_SPECIFIED);
      if (!AudioSystem.isLineSupported(info)) {
	//System.out.println("Line " + info + " not supported.");
	return;
      }

      try{
	outputLine = (SourceDataLine) AudioSystem.getLine(info);
	//outputLine.addLineListener(this);
	outputLine.open(audioFormat);
      } 
      catch (LineUnavailableException ex) { 
	System.out.println("Unable to open the sourceDataLine: " + ex);
        return;
      } 
      catch (IllegalArgumentException ex) { 
	System.out.println("Illegal Argument: " + ex);
	return;
      }

      frameSizeInBytes = audioFormat.getFrameSize();
      int bufferLengthInFrames = outputLine.getBufferSize()/frameSizeInBytes/2;
      bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;

      //buffer = new byte[bufferLengthInBytes];
      //if(originalClassLoader!=null)
      //  Thread.currentThread().setContextClassLoader(originalClassLoader);

      this.rate=rate;
      this.channels=channels;
    }
    catch(Exception ee){
      System.out.println(ee);
    }
  }

    protected void playStream (InputStream stream)
    {
        init_jorbis();

    loop:
        while (true) {
            int eos = 0;

            int index = oy.buffer(BUFSIZE);
            buffer = oy.data;
            try {
                bytes = stream.read(buffer, index, BUFSIZE);
            } catch (Exception e) {
                System.err.println(e);
                return;
            }
            oy.wrote(bytes);
        
            if (oy.pageout(og) != 1) {
                if (bytes < BUFSIZE) {
                    break;
                }
                System.err.println("Input does not appear to be an Ogg bitstream.");
                return;
            }

            os.init(og.serialno());
            os.reset();

            vi.init();
            vc.init();

            if (os.pagein(og) < 0) {
                // error; stream version mismatch perhaps
                System.err.println("Error reading first page of Ogg bitstream data.");
                return;
            }

            if (os.packetout(op) != 1) {
                // no page? must not be vorbis
                System.err.println("Error reading initial header packet.");
                break;
                //      return;
            }

            if (vi.synthesis_headerin(vc, op) < 0) { 
                // error case; not a vorbis header
                System.err.println("This Ogg bitstream does not contain Vorbis audio data.");
                return;
            }

            int i=0;

            while (i < 2) {
                while (i < 2) {
                    int result = oy.pageout(og);
                    if (result==0) {
                        break; // Need more data
                    }
                    if (result==1) {
                        os.pagein(og);
                        while (i < 2) {
                            result = os.packetout(op);
                            if (result == 0) {
                                break;
                            }
                            if (result == -1) {
                                System.err.println("Corrupt secondary header.  Exiting.");
                                //return;
                                break loop;
                            }
                            vi.synthesis_headerin(vc, op);
                            i++;
                        }
                    }
                }

                index = oy.buffer(BUFSIZE);
                buffer = oy.data; 
                try {
                    bytes = stream.read(buffer, index, BUFSIZE);
                }
                catch(Exception e){
                    System.err.println(e);
                    return;
                }

                if (bytes == 0 && i < 2) {
                    System.err.println("End of file before finding all Vorbis headers!");
                    return;
                }
                oy.wrote(bytes);
            }

            convsize=BUFSIZE/vi.channels;

            vd.synthesis_init(vi);
            vb.init(vd);

            double[][][] _pcm=new double[1][][];
            float[][][] _pcmf=new float[1][][];
            int[] _index=new int[vi.channels];

            getOutputLine(vi.channels, vi.rate);

            while (eos == 0) {
                while (eos == 0) {

                    if (_player != Thread.currentThread()) {
                        //System.err.println("bye.");
                        try {
                            //outputLine.drain();
                            //outputLine.stop();
                            //outputLine.close();
                            stream.close();
                        } catch(Exception ee) {
                        }
                        return;
                    }

                    int result = oy.pageout(og);
                    if (result == 0) {
                        break; // need more data
                    }
                    if (result == -1) { // missing or corrupt data at this page position
                    //	    System.err.println("Corrupt or missing data in bitstream; continuing...");

                    } else {
                        os.pagein(og);
                        while (true) {
                            result = os.packetout(op);
                            if (result == 0) break; // need more data
                            if (result == -1) { // missing or corrupt data at this page position
                                // no reason to complain; already complained above
                            } else {
                                // we have a packet.  Decode it
                                int samples;
                                if (vb.synthesis(op) == 0) { // test for success!
                                    vd.synthesis_blockin(vb);
                                }
                                while ((samples =
                                    vd.synthesis_pcmout(_pcmf, _index)) > 0) {

                                    double[][] pcm = _pcm[0];
                                    float[][] pcmf = _pcmf[0];
                                    boolean clipflag = false;
                                    int bout = Math.min(samples, convsize);

                                    // convert doubles to 16 bit signed ints (host order) and
                                    // interleave
                                    for (i = 0; i < vi.channels; i++) {
                                        int ptr = i*2;
                                        //int ptr=i;
                                        int mono = _index[i];
                                        for (int j = 0; j < bout; j++) {
                                            int val = (int)
                                                (pcmf[i][mono+j] * 32767.);
                                            if (val > 32767){
                                                val = 32767;
                                                clipflag = true;

                                            } else if (val < -32768) {
                                                val = -32768;
                                                clipflag = true;
                                            }
                                            if (val < 0) {
                                                val = val | 0x8000;
                                            }
                                            convbuffer[ptr] = (byte)(val);
                                            convbuffer[ptr+1] = (byte)(val>>>8);
                                            ptr += 2 * (vi.channels);
                                        }
                                    }
                                    outputLine.write(convbuffer, 0,
                                        2 * vi.channels * bout);
                                    vd.synthesis_read(bout);
                                }
                            }
                        }
                        if (og.eos() != 0) {
                            eos=1;
                        }
                    }
                }

                if (eos==0) {
                    index = oy.buffer(BUFSIZE);
                    buffer = oy.data;
                    try {
                        bytes = stream.read(buffer,index,BUFSIZE);

                    } catch (Exception e) {
                        System.err.println(e);
                        return;
                    }
                    if (bytes == -1) {
                        break;
                    }
                    oy.wrote(bytes);
                    if (bytes==0) {
                        eos=1;
                    }
                }
            }

            os.clear();
            vb.clear();
            vd.clear();
            vi.clear();
        }

        oy.clear();

        //System.err.println("Done.");

        try {
            if (stream != null) {
                stream.close();
            }
        } catch (Exception e) {
        }
    }

    public void setVolume (float volume)
    {
        // TODO
    }

    protected Thread _player;
}
