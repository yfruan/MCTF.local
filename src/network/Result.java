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
package network;

import java.util.HashMap;
import java.util.Map;

/**
 * Result after sending reliable message
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Result {
	private int flag;
	private Object data=null;
	
	// result flag
	public final static int RECEIVED=0;			// the message has been received
	public final static int REPLIED=1;			// the message has been replied
	public final static int TIMEOUT=2;			// the message has not reached the destination
	public final static int EXTRA_ERROR=3;		// extra error
	
	// for creating custom types of results
	private static Map<String,Integer> customFlags=new HashMap<>();
	private static int flagCount=4;
	
	/**
	 * Register custom flag to explain result type
	 * @param flag the result flag
	 */
	public static void registerCustomFlag(String flag){
		customFlags.put(flag, flagCount++);
	}
	
	/**
	 * Return custom flag
	 * @param flag the flag
	 * @return the custom flag identifier
	 */
	public static int customFlag(String flag){
		if(!customFlags.containsKey(flag))
			return -1;
		return customFlags.get(flag);	
	}
	
	/**
	 * Constructor
	 * @param flag the result flag
	 */
	public Result(int flag){
		this.flag=flag;
	}
	
	/**
	 * Constructor
	 * @param flag the result flag
	 * @param data the result data
	 */
	public Result(int flag, Object data) {
		this.flag = flag;
		this.data = data;
	}
	
	/**
	 * Get the result flag
	 * @return the result flag
	 */
	public int getFlag(){
		return this.flag;
	}
	
	/**
	 * Get the result data
	 * @return the result data
	 */
	public Object getData(){
		return this.data;
	}
	
}
