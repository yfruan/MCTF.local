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
import network.Handler;
import network.RUDPImpl;
import network.Result;
import network.address.Endpoint;
import network.protocol.Message;

/**
 * Abstract controller of channel controllers
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public abstract class AbstractController {
	private String userId;
	private RUDPImpl mainRUDP;
	private Endpoint remoteEndpoint;
		
	protected void configure(String userId,RUDPImpl mainRUDP){
		this.userId=userId;
		this.mainRUDP=mainRUDP;
	}
	
	protected void setRemoteEndpoint(Endpoint remoteEndpoint){
		this.remoteEndpoint=remoteEndpoint;
	}
	
	/**
	 * Register specific handler of controller
	 */
	protected abstract void registerControllerHandler();
	
	protected void registerHandler(int event, Handler handler){
		this.mainRUDP.getReactor().register(event, handler);
	}
	
	protected void sendMessage(int event, byte[] payload){
		Message message=new Message(userId,event,payload);
		this.mainRUDP.sendMessage(message,remoteEndpoint);
	}
	
	protected Result sendReliableMessage(int event, byte[] payload, Handler handler){
		Message message=new Message(userId,event,payload);
		return mainRUDP.sendReliableMessage(message, remoteEndpoint, null, handler);	
	}
}
