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
package network.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Message event
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Event {
	// six default message events
	public final static int STUN=0;
	public final static int TURN=1;
	public final static int CONNECT=2;
	public final static int VIDEO=3;
	public final static int AUDIO=4;
	public final static int TOUCH=5;
	
	public final static String[] defaultEvents={"STUN","TURN", "CONNECT","VIDEO","AUDIO","TOUCH"};

	// for registering custom events
	private static Map<String,Integer> customEvents=new HashMap<>();  
	private static int eventCount=6; 
		
	/**
	 * Register the custom event
	 * @param event the custom event
	 * @return the custom event identifier
	 */
	public static int registerCustomEvent(String event){
		customEvents.put(event, eventCount);
		return eventCount++;
	}
	
	/**
	 * Get the custom event identifier
	 * @param event the custom event 
	 * @return the identifier
	 */
	public static int customEvent(String event){
		if(!customEvents.containsKey(event))
			return -1;
		return customEvents.get(event);
	}
}
