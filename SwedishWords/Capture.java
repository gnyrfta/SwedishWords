  /*
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free,
 * license to use, modify and redistribute this software in 
 * source and binary code form, provided that i) this copyright
 * notice and license appear on all copies of the software; and 
 * ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty
 * of any kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS
 * AND WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE 
 * HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR 
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT
 * WILL SUN OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, 
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS
 * OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY
 * TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGES.

 This software is not designed or intended for use in on-line
 control of aircraft, air traffic, aircraft navigation or
 aircraft communications; or in the design, construction,
 operation or maintenance of any nuclear facility. Licensee 
 represents and warrants that it will not use or redistribute 
 the Software for such purposes.
 */

/*  The above copyright statement is included because this 
 * program uses several methods from the JavaSoundDemo
 * distributed by SUN. In some cases, the sound processing methods
 * unmodified or only slightly modified.
 * All other methods copyright Steve Potts, 2002
 */
 /*This class was slightly altered by David Jacobsson for use in the program 
 SwedishWords. */
 
 
package SwedishWords;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;

  class Capture implements Runnable {

		String errStr;
		TargetDataLine line;
    Thread thread;  
 	  double duration, seconds;
    
    public void start() {
      errStr = null;
      thread = new Thread(this);
      thread.setName("Capture");
      thread.start();
    }

    public void stop() {
      thread = null;
    }

    private void shutDown(String message) {
      if ((errStr = message) != null && thread != null) {
        thread = null;
        MainWindowPronounce.playB.setEnabled(true);
        MainWindowPronounce.captB.setText("Record");
        System.err.println(errStr);
      }
    }

    public void run() {
      duration = 0;
      MainWindowPronounce.audioInputStream = null;
      // define the required attributes for our line,
      // and make sure a compatible line is supported.
      AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
      float rate = 44100.0f;
      int channels = 2;
      int frameSize = 4;
      int sampleSize = 16;
      boolean bigEndian = true;
      AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8)
          * channels, rate, bigEndian);
      DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
      if (!AudioSystem.isLineSupported(info)) {
        shutDown("Line matching " + info + " not supported.");
        return;
      }
      // get and open the target data line for capture.
      try {
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format, line.getBufferSize());
      } catch (LineUnavailableException ex) {
        shutDown("Unable to open the line: " + ex);
        return;
      } catch (SecurityException ex) {
        shutDown(ex.toString());
        //JavaSound.showInfoDialog();
        return;
      } catch (Exception ex) {
        shutDown(ex.toString());
        return;
      }
      // play back the captured audio data
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int frameSizeInBytes = format.getFrameSize();
      int bufferLengthInFrames = line.getBufferSize() / 8;
      int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;
      byte[] data = new byte[bufferLengthInBytes];
      int numBytesRead;
      line.start();
      while (thread != null) {
        if ((numBytesRead = line.read(data, 0, bufferLengthInBytes)) == -1) {
          break;
        }
        out.write(data, 0, numBytesRead);
      }
      // we reached the end of the stream.
      // stop and close the line.
      line.stop();
      line.close();
      line = null;

      // stop and close the output stream
      try {
        out.flush();
        out.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      
      // load bytes into the audio input stream for MainWindowPronounce.playBack

      byte audioBytes[] = out.toByteArray();
      ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
      MainWindowPronounce.audioInputStream = new AudioInputStream(bais, format, audioBytes.length / frameSizeInBytes);

      long milliseconds = (long) ((MainWindowPronounce.audioInputStream.getFrameLength() * 1000) / format
          .getFrameRate());
      duration = milliseconds / 1000.0;

      try {
        MainWindowPronounce.audioInputStream.reset();
      } catch (Exception ex) {
        ex.printStackTrace();
        return;
      }

    }
  } // End class Capture
