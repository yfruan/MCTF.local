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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import network.Handler;
import network.assist.Serialization;
import network.protocol.Event;
import network.protocol.TouchFlag;
import network.protocol.Payload;

/**
 * Controller for "TOUCH"
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class TouchController extends AbstractController{	
	private ArrayList<String> currentPoints;       
	private List<PathConfigHook> inPathConfigHooks=new ArrayList<>();
	private List<PathAddHook> inPathAddHooks=new ArrayList<>();
	
	/**
	 * Constructor
	 */
	public TouchController(){	
	}
	
	/**
	 * Insert a hook for handling incoming information for configuring next touch paths
	 * @param hook the path configure hook
	 */
	public void insertInPathConfigHook(PathConfigHook hook){
		this.inPathConfigHooks.add(hook);
	}
	
	/**
	 * Insert a hook for handling incoming information for adding touch paths
	 * @param hook the path add hook
	 */
	public void insertInPathAddHook(PathAddHook hook){
		this.inPathAddHooks.add(hook);
	}
	
	/**
	 * Register touch controller handler
	 */
	public void registerControllerHandler(){
		Handler handler=(message)->{				
			Payload payload= (Payload)Serialization.deserialize(message.getPayload());	
			switch(payload.getFlag()){
				case TouchFlag.CONFIG:{
					@SuppressWarnings("unchecked")
					HashMap<String,String> configuration=(HashMap<String, String>) Serialization.deserialize((byte[])payload.getData());
					inPathConfigHooks.forEach(inHook->{
						inHook.execute(configuration);
					});
					break;
				}
				case TouchFlag.ADD:{
					@SuppressWarnings("unchecked")
					ArrayList<String> path=(ArrayList<String>) Serialization.deserialize((byte[])payload.getData());
					ArrayList<TouchPoint> points=new ArrayList<>();
					for(String str:path)
						points.add(new TouchPoint(str));
					inPathAddHooks.forEach(inHook->{
						inHook.execute(points);
					});
					break;
				}
				case TouchFlag.REMOVE:{
					break;
				}
				default: break;
				}
			return null;
		};
		this.registerHandler(Event.TOUCH, handler);
	}
	
	
	/**
	 * Set configuration
	 * @param configuration path configuration
	 */
	public void configure(HashMap<String,String> configuration){		
		this.sendReliableMessage(Event.TOUCH, Serialization.serialize(
				new Payload(TouchFlag.CONFIG, Serialization.serialize(configuration))),null);
	}
	
	/**
	 * Initialize path, setting starting point
	 * @param x  x coordinate value of starting point
	 * @param y  y coordinate value of starting point
	 */
	public void startPath(double x,double y){
		currentPoints=new ArrayList<>();
        this.currentPoints.add(new TouchPoint(x,y).toString());
	}
	
	/**
	 * Extend point
	 * @param x  x coordinate value of point
	 * @param y  y coordinate value of point
	 */
	public void extendPath(double x,double y){
        this.currentPoints.add(new TouchPoint(x,y).toString());
	}
	
	/**
	 * End path and send "PATH" message
	 */
	public void endPath(){
		this.sendReliableMessage(Event.TOUCH, Serialization.serialize(
				new Payload(TouchFlag.ADD,Serialization.serialize(currentPoints))),null);
	}
	
	/**
	 * TouchPoint with variables x and y
	 * @author Yifan Ruan (ry222ad@student.lnu.se)
	 */
	public class TouchPoint{		
		private double x;
		private double y;
		public TouchPoint( double x,  double y){
			this.x=x;
			this.y=y;
		}
		
		public TouchPoint(String str){
			this.x=Double.parseDouble(str.split(",")[0]);
			this.y=Double.parseDouble(str.split(",")[1]);
		}

		public double getX(){
			return x;
		}
		
		public double getY(){
			return y;
		}
		
		public String toString(){
			return x+","+y;
		}
	}
	
	/**
	 * Definition of path configure hook
	 * @author Yifan Ruan (ry222ad@student.lnu.se)
	 */
	@FunctionalInterface
	public interface PathConfigHook{
		public void execute(HashMap<String,String> configuration);
	}
	
	/**
	 * Definition of path add hook
	 * @author Yifan Ruan (ry222ad@student.lnu.se)
	 */
	@FunctionalInterface
	public interface PathAddHook{
		public void execute(ArrayList<TouchPoint> points);
	}
}
