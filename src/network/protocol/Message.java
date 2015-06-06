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

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Definition of the formatted message to be delivered
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Message implements Serializable,KryoSerializable{
	private static final long serialVersionUID = 1L;
	
	private boolean isReliable=false;			// the message reliable or not
	private int id=-1;					// the unique identifier                   
	private String senderId;					// who sending the message
	
	private int code=-1;       					// message type
	public final static int GENERAL=0;
	public final static int ACK=1;
	public final static int PING=2;
	public final static int REPLY=3;
	private int repliedMessageId=-1;  			// if type of the message is REPLAY or ACK, set this field, otherwise equal to-1
	
	private int event=-1;      					// message event
	private byte[] payload=null;				// actual data 
	
	private static AtomicInteger count=new AtomicInteger(0);   // message number count
		
	@Override
	public void read(Kryo kryo, Input input) {
		this.isReliable=input.readBoolean();
		this.id=input.readInt();
		this.senderId=input.readString();
		this.code=input.readInt();
		this.repliedMessageId=input.readInt();
		this.event=input.readInt();
		this.payload=(byte[])kryo.readClassAndObject(input);
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeBoolean(isReliable);
		output.writeInt(id);
		output.writeString(senderId);
		output.writeInt(code);
		output.writeInt(repliedMessageId);
		output.writeInt(event);
		kryo.writeClassAndObject(output, payload);
	}
	
	public Message(){}
	
	/**
	 * Constructor
	 * @param senderId the sender id
	 * @param event the message event
	 * @param payload the message payload, carrying the actual data
	 */
	public Message(String senderId,int event, byte[] payload){
		this.senderId=senderId;
		this.code=Message.GENERAL;
		this.event=event;
		this.payload=payload;
		this.id=count.getAndIncrement();
	}
	
	/**
	 * Constructor
	 * @param senderId the sender id
	 * @param code the message code 
	 * @param repliedMessageId the message id to be replied
	 * @param payload the message payload, carrying the actual data
	 */
	public Message(String senderId, int code, int repliedMessageId, byte[] payload){
		this.senderId=senderId;
		this.code=code;
		this.repliedMessageId=repliedMessageId;
		this.payload=payload;
		this.id=count.getAndIncrement();
	}
	
	/**
	 * Get the message code
	 * @return the message code
	 */
	public int getCode(){
		return this.code;
	}
	
	/**
	 * Get the replied message id
	 * @return the replied message id
	 */
	public int getRepliedMessageId(){
		return this.repliedMessageId;
	}
	
	/**
	 * Set the message reliable
	 */
	public void setReliable(){
		this.isReliable=true;
	}
	
	/**
	 * Check the message reliable or not
	 * @return true if the message reliable otherwise false
	 */
	public boolean isReliable(){
		return this.isReliable;
	}
	
	/**
	 * Get the message id
	 * @return the message id
	 */
	public int getId() {
		return id;
	}

	/**
	 * Get the message event
	 * @return the message event
	 */
	public int getEvent() {
		return event;
	}

	/**
	 * Get the message payload
	 * @return the message payload
	 */
	public byte[] getPayload() {
		return payload;
	}
	
	/**
	 * Get the sender id
	 * @return the sender id
	 */
	public String getSenderId() {
		return senderId;
	}
}
