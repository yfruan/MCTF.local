/*******************************************************************************
 * Copyright Yifan Ruan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package controller;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import network.Handler;
import network.protocol.Event;

/**
 * Controller for "AUDIO" 
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class AudioController extends AbstractController{	
	// parameters of audio
	private AudioFormat format;
	private AudioInputStream inputStream;
	private TargetDataLine targetDataLine;
	private SourceDataLine sourceLine;
	
	private boolean isAudioStopped=true;
	private int bufferSize;
		
	private List<AudioHook> inHooks=new ArrayList<>();   // executed when extracting "AUDIO" payload after receiving message
	private List<AudioHook> outHooks=new ArrayList<>(); // executed when getting "AUDIO" payload before sending message
	
	/**
	 * Constructor
	 */
	public AudioController(){
		format = getAudioFormat();
		bufferSize=(int) format.getSampleRate()* format.getFrameSize();
		this.setDefaultInHook();
	}
	
	/**
	 * Insert an audio hook for executing incoming audio data
	 * @param hook the audio hook
	 */
	public void insertInHook(AudioHook hook){
		this.inHooks.add(hook);
	}
	
	/**
	 * Insert an audio hook for executing outgoing audio data
	 * @param hook the audio hook
	 */
	public void insertOutHook(AudioHook hook){
		this.outHooks.add(hook);
	}
	
	/**
	 * Get audio format
	 * @return the audio format
	 */
	private AudioFormat getAudioFormat() {
		AudioFormat.Encoding encoding=AudioFormat.Encoding.PCM_SIGNED;
	    float sampleRate = 8000.0F;
	    int sampleSizeInbits = 16;
	    int channels = 1;
	    int frameSize =2;
	    float frameRate=8000.0F;
	    boolean bigEndian = false;
	    return new AudioFormat(encoding,sampleRate, sampleSizeInbits, channels, frameSize, frameRate,bigEndian);
	}
	
	/**
	 * Set default inHook as speaker
	 */
	private void setDefaultInHook(){
		AudioHook inHook=(format, audioData)->{
			try{
				InputStream byteInputStream = new ByteArrayInputStream(audioData);
				inputStream = new AudioInputStream(byteInputStream, format, audioData.length / format.getFrameSize());
				DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, format);
				sourceLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
				sourceLine.open(format);
				sourceLine.start();
				
	        	new Thread(new PlayVoice()).start();
			}catch(Exception e){
				e.printStackTrace();
			}          
		};
		this.insertInHook(inHook);
	}
	
	/**
	 * Register audio controller handler
	 */
	public void registerControllerHandler(){
		Handler handler=(message)->{					
	        inHooks.forEach(inHook->{
	        	inHook.execute(format, message.getPayload());
	        });
			return null;
		};
		this.registerHandler(Event.AUDIO, handler);
	}
	
	/**
	 * Start audio and send "AUDIO" message
	 */
	public void start(){
		this.isAudioStopped=false;
		Runnable audioTransmit=new Runnable(){
			@Override
			public void run() {
				try{
			        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
			        targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
			        targetDataLine.open(format);
			        targetDataLine.start();
			        
			        ByteArrayOutputStream out  = new ByteArrayOutputStream();
			        int numBytesRead;

			        byte tempBuffer[] = new byte[bufferSize];
			    	while (!isAudioStopped) {
			    		numBytesRead= targetDataLine.read(tempBuffer, 0, tempBuffer.length);
					    if (numBytesRead > 0) {  
					    	out.write(tempBuffer, 0, numBytesRead);
					    	sendMessage(Event.AUDIO,tempBuffer);		    				
		    				outHooks.forEach(outHook->{
								outHook.execute(format,tempBuffer);
		    				});
					    }
			    	}
			    }
			    catch(Exception e){
			    	e.printStackTrace();
			    } 
			}			
		};
		new Thread(audioTransmit).start();
	}
	
	/**
	 * Stop audio
	 */
	public void stop(){
		isAudioStopped=true;
		if(targetDataLine.isOpen())
			targetDataLine.close();
	}
	
    class PlayVoice implements Runnable {    	
    	byte[] tempBuffer=new byte[bufferSize];
        public void run() {
            try {
                int numBytesRead;
                while ((numBytesRead = inputStream.read(tempBuffer, 0, tempBuffer.length)) != -1) {
                    if (numBytesRead > 0) {
                        sourceLine.write(tempBuffer, 0, numBytesRead);
                    }
                }
            } catch (Exception e) {
            	e.printStackTrace();
                System.exit(0);
            }
        }
    }
    
    /**
     * Definition of audio hook
     * @author Yifan Ruan (ry222ad@student.lnu.se)
     */
	@FunctionalInterface
	public interface AudioHook{
		public void execute(AudioFormat format, byte[] audioData);
	}
    
}
