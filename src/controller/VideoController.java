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

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import network.Handler;
import network.protocol.Event;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Rotation;
import com.github.sarxos.webcam.*;

/**
 * Controller for "VIDEO"
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */

public class VideoController extends AbstractController{
	private Webcam webcam;
	private boolean rotated=false;
	
	private boolean isVideoStopped=true;     
	private boolean isVideoPaused=false;
	
	private List<VideoHook> inHooks=new ArrayList<>();   // executed when extracting "VIDEO" payload after receiving message
	private List<VideoHook> outHooks=new ArrayList<>(); //  executed when getting "VIDEO" payload before sending message

	private int period=300;      // period to capture image
	private float quality=.2f;   // image quality, in terms of compression level, the high the better
	
	/*
	 * Constructor
	 */
	public VideoController(){
	}
	
	/**
	 * Insert a video hook for handling incoming video data
	 * @param hook  the video hook
	 */
	public void insertInHook(VideoHook hook){
		this.inHooks.add(hook);
	}
	
	/**
	 * Insert a video hook for handling outgoing video data
	 * @param hook the video hook
	 */
	public void insertOutHook(VideoHook hook){
		this.outHooks.add(hook);
	}
	
	/**
	 * Pause video
	 */
	public synchronized void setVideoPaused(){
		this.isVideoPaused=!this.isVideoPaused;
	}
	
	/**
	 * Set period of capturing image
	 * @param period the period time
	 */
	public void setPeriod(int period){
		this.period=period;
	}
	
	/**
	 * Set quality of captured image
	 * @param quality the image quality
	 */
	public void setQuality(float quality){
		this.quality=quality;
	}
	
	/**
	 * Set webcam
	 * @param webcamIndex index of used webcam
	 * @param resolution  only supports "480p","720p"
	 */
	public void setWebcam(int webcamIndex,String resolution){
		webcam =  Webcam.getWebcams().get(webcamIndex);
		if(resolution.equals("480p")){
			webcam.setViewSize(new Dimension(640, 480));
		}
		else if(resolution.equals("720p")){
			webcam.setCustomViewSizes(new Dimension[] { WebcamResolution.HD720.getSize()});
			webcam.setViewSize(WebcamResolution.HD720.getSize()); 
		}
		else
			System.out.println("Wrong resolution");
	}
	
	/**
	 * Rotate the image
	 */
	public void setRotated(){
		this.rotated=true;
	}
	
	/**
	 * Register video controller handler
	 */
	public void registerControllerHandler(){
		Handler handler=(message)->{
			try{				
				BufferedImage bufferedImage=ImageIO.read(new ByteArrayInputStream(message.getPayload()));
				if(bufferedImage!=null)
					inHooks.forEach(inHook->{
							inHook.execute(bufferedImage);
					});
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			return null; 
		};
		this.registerHandler(Event.VIDEO, handler);
	}
	
	/**
	 * Capture image and send "VIDEO" message
	 */
	public void start() {
		webcam.open();
		this.isVideoStopped=false;
		
		Runnable videoTransmit = new Runnable(){
			@Override
			public void run() {
				BufferedImage bufferedImage;
				while (!isVideoStopped) {
					try {						
					    if(!isVideoPaused){
					    	
						if ((bufferedImage = webcam.getImage()) != null) {
							
							// rotate the image to the proper angle degree
							if(rotated)
								bufferedImage=Scalr.rotate(bufferedImage, Rotation.CW_270);
							
							ByteArrayOutputStream out = new ByteArrayOutputStream();
						    ImageOutputStream ios = ImageIO.createImageOutputStream(out);
							          
						    // compress the image to "jpeg" type
						    Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
						    ImageWriter writer = writers.next();
						            
						    // set quality of image
						    ImageWriteParam param = writer.getDefaultWriteParam();
						    param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
						    param.setCompressionQuality(quality);
						    writer.setOutput(ios);
						    writer.write(null, new IIOImage(bufferedImage, null, null), param);	
						    writer.dispose(); 	
						    
							byte tempBuffer[]=out.toByteArray();
														
							out.flush();											    					    
							out.close();
							sendMessage(Event.VIDEO,tempBuffer);
							BufferedImage bImage=bufferedImage;
							outHooks.forEach(outHook->{
								outHook.execute(bImage);
							});
						}
					  }    
					Thread.sleep(period);
					} catch (Exception e) {
						e.printStackTrace();
					} 
				}
			}
			
		};
		new Thread(videoTransmit).start();
	}
	
	/**
	 * Stop webcam
	 */
	public void stop(){
		isVideoStopped=true;
		rotated=false;
		isVideoPaused=false;
		if(webcam.isOpen())
			webcam.close();
	}
	
	/**
	 * Definition of video hook
	 * @author Yifan Ruan (ry222ad@student.lnu.se)
	 */
	@FunctionalInterface
	public interface VideoHook{
		public void execute(BufferedImage bufferedImage);
	}

}
