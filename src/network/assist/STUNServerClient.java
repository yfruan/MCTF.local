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
package network.assist;

import java.net.InetAddress;
import network.Handler;
import network.Result;
import network.RUDPImpl;
import network.address.Endpoint;
import network.address.NetworkInfo;
import network.protocol.Event;
import network.protocol.Message;
import network.protocol.Payload;
import network.protocol.STUNFlag;

/**
 * Client for STUN Server
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class STUNServerClient {
	private String userId;
	private Endpoint STUNServerEndpoint;
	private RUDPImpl rudpImpl;
	
	/**
	 * Constructor
	 * @param userId the user id
	 * @param rudpImpl the instance of "RUDPImpl" class, for sending messages
	 * @param STUNServerEndpoint the STUN server endpoint
	 */
	public STUNServerClient(String userId, RUDPImpl rudpImpl,Endpoint STUNServerEndpoint){
		this.userId=userId;
		this.rudpImpl=rudpImpl;
		this.STUNServerEndpoint=STUNServerEndpoint;
	}
	
	/**
	 * Register user to STUN Server
	 * @param  hostAddress  host address 
	 * @param  hostPort     host port
	 * @return  true if success else false
	 */
	public boolean register(InetAddress hostAddress, int hostPort){
		Endpoint endpoint=new Endpoint(hostAddress,hostPort);
		Payload payload=new Payload(STUNFlag.REGISTER,endpoint); 
		Message message=new Message(userId,Event.STUN,Serializer.write(payload));
		Result result=this.rudpImpl.sendReliableMessage(message,STUNServerEndpoint,null,null);
		return result.getFlag()==Result.RECEIVED;
	}
	
	/**
	 * Unregister user to STUN Server
	 * @return true if success else false
	 */
	public boolean unregister(){
		Message message=new Message(userId,Event.STUN,Serializer.write(new Payload(STUNFlag.UNREGISTER,null)));
		Result result=this.rudpImpl.sendReliableMessage(message,STUNServerEndpoint,null,null);
		return result.getFlag()==Result.RECEIVED;
	}
		
	/**
	 * Get specific user network information, including private and public endpoints
	 * @param remoteUserId  requested user
	 * @return returned network endpoints
	 */
	public NetworkInfo getInfo(String remoteUserId){
		String[] userIds={remoteUserId};
		NetworkInfo[] infos=this.getInfo(userIds);
		return infos==null?null:infos[0];
	}
	
	/**
	 * Get network information of list of users
	 * @param remoteUserIds requested list of user ids
	 * @return returned list of network endpoints
	 */
	public NetworkInfo[] getInfo(String[] remoteUserIds){
		Message message=new Message(userId,Event.STUN,Serializer.write(new Payload(STUNFlag.GETINFO,remoteUserIds)));
		Handler handler= (reply)->{			
	    	Payload payload= (Payload)Serializer.read(reply.getPayload(),Payload.class);
			if(payload.getFlag()==STUNFlag.GETINFO){
				return payload.getData();
			}
			return null;
	    };
	    Result result=this.rudpImpl.sendReliableMessage(message, STUNServerEndpoint,null,handler);
	    if(result.getFlag()==Result.REPLIED){
	    	return (NetworkInfo[])result.getData();
	    }	    
		return null;
	}
}
