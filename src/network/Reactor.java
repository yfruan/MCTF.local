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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import network.Handler;
import network.protocol.Message;

/**
 * Dispatching the message to proper handler depending on the event
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Reactor {
	private Map<Integer,Handler> handlers=new ConcurrentHashMap<>();			// registered handlers
	private ExecutorService executorService = Executors.newCachedThreadPool();
	
	/**
	 * Register handler
	 * @param event  which message to be handled
	 * @param handler object to handle message
	 */
	public void register(int event,Handler handler){
		this.handlers.put(event, handler);
	}
	
	/**
	 * Unregister handler
	 * @param event which message to be handled
	 */
	public void unregister(int event){
		this.handlers.remove(event);
	}
	
	/**
	 * Dispatch incoming message
	 * @param message incoming message
	 */
	public void dispatch(Message message){
		executorService.execute(new Runnable() {
		    public void run() {
				int event=message.getEvent();
				if(handlers.containsKey(event))
					handlers.get(event).handle(message);		    
			}
		});
	}
	
	/**
	 * Close
	 */
	public void shutdown(){
		this.executorService.shutdown();
	}
}
