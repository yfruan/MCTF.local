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
import network.Handler;
import network.RUDPImpl;
import network.Result;
import network.address.Endpoint;
import network.protocol.Event;
import network.protocol.Message;
import network.protocol.Payload;
import network.protocol.TURNFlag;

public class TURNServerClient {
	private String userId;
	private Endpoint TURNServerEndpoint;
	private RUDPImpl rudpImpl;
	
	/**
	 * Constructor
	 * @param userId the user id
	 * @param rudpImpl the instance of "RUDPImpl" class, for sending messages
	 * @param TURNServerEndpoint the TURN server endpoint
	 */
	public TURNServerClient(String userId, RUDPImpl rudpImpl,Endpoint TURNServerEndpoint){
		this.userId=userId;
		this.rudpImpl=rudpImpl;
		this.TURNServerEndpoint=TURNServerEndpoint;
	}
			
	/**
	 * Request to relay messages
	 * @param remoteEndpoint remote endpoint to connect with
	 * @return true if success else false
	 */
	public boolean relay(Endpoint remoteEndpoint){
		Message message=new Message(userId,Event.TURN,Serialization.serialize(new Payload(TURNFlag.RELAY,remoteEndpoint)));
		Handler handler= (reply)->{
	    	Payload payload= (Payload)Serialization.deserialize(reply.getPayload());
			if(payload.getFlag()==TURNFlag.RELAY){
				return payload.getData();
			}
			return false;
	    };	
		Result result=this.rudpImpl.sendReliableMessage(message,TURNServerEndpoint,null,handler);	
		return result.getFlag()==Result.REPLIED && (boolean) result.getData();
	}
	
	/**
	 * Request to stop relaying messages
	 * @return true if success else false
	 */
	public boolean unrelay(){
		Message message=new Message(userId,Event.TURN,Serialization.serialize(new Payload(TURNFlag.UNRELAY,null)));
		Result result=this.rudpImpl.sendReliableMessage(message,TURNServerEndpoint,null,null);
		return result.getFlag()==Result.RECEIVED;
	}

}
