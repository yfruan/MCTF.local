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

import java.io.EOFException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import log.MessageLog;
import network.address.Endpoint;
import network.assist.Serializer;
import network.protocol.Message;

/**
 * Reliable UDP implementation
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class RUDPImpl implements Runnable{

	private String userId;						// specific identifier
	
	private Reactor reactor;					// who dispatches incoming messages
	
    private ExecutorService executorService;   // the thread pool of managing running threads

	
	private int port;						   // UDP port		
	private DatagramSocket socket;  		   // UDP socket
	private final int MAX_PACKET_SIZE=32000;   // max packet size
	private boolean isStopped=false;           // identifier of stopping
    
    private Set<Integer> ACKSet = Collections.synchronizedSet(new HashSet<>());  // ACK message record
    private Map<Integer,Message> replys=new ConcurrentHashMap<>();           	 // reply message record
    
    private Set<Endpoint> keepAliveEndpoints = Collections.synchronizedSet(new HashSet<>()); // record of endpoints for keeping alive
    
    // default reliable configuration
    private final static int ACK_TIMEOUT=5000;
    private final static int REPLY_TIMEOUT=45000;
    private final static int RESEND_NUM=2;
    private ReliableConfiguration defaultConfig=new ReliableConfiguration(ACK_TIMEOUT,REPLY_TIMEOUT,RESEND_NUM);

    /**
     * Constructor
     * @param userId the user id
     * @param port the host port
     */
	public RUDPImpl(String userId,int port){
		this.userId=userId;
		this.port=port;
		this.reactor=new Reactor();
        try {
			this.socket = new DatagramSocket(this.port);
			this.executorService = Executors.newFixedThreadPool(10);
			 //this.executorService = Executors.newCachedThreadPool();
		} catch (SocketException e) {
			e.printStackTrace();
		}
        
        // start log
        MessageLog.start();
	}
	
	/**
	 * Get the reactor
	 * @return reactor to dispatch message
	 */
	public Reactor getReactor(){
		return this.reactor;
	}
	
	/**
	 * Add a endpoint to be kept alive
	 * @param endpoint which to be kept alive
	 */
	public void addKeepAliveEndpoint(Endpoint endpoint){
		this.keepAliveEndpoints.add(endpoint);
		synchronized(keepAliveEndpoints){
			keepAliveEndpoints.notifyAll();
		}
	}
	
	/**
	 * Remove the endpoint to be kept alive
	 * @param endpoint which not to be kept alive
	 */
	public void removeKeepAliveEndpoint(Endpoint endpoint){
		this.keepAliveEndpoints.remove(endpoint);
	}
		
	/**
	 * Listen for incoming messages
	 */
	@Override
	public void run() {	
		byte[] receivedData = new byte[MAX_PACKET_SIZE];
		int length=receivedData.length;
		
		while (!isStopped()) {
		    DatagramPacket receivedPacket = new DatagramPacket(receivedData, length);
        	try {
				socket.receive(receivedPacket);
	        	//System.out.println("Message received");
            	Message message=(Message) Serializer.read(receivedPacket.getData(),Message.class);
            	
       		 	// log received messages
       		 	MessageLog.info(MessageLog.RECEIVED, message);
            	
        		// response ACK message
        		if(message.isReliable()){
        			Message ACKMessgae=new Message(userId,Message.ACK,message.getId(),null);
        			this.sendMessage(ACKMessgae,new Endpoint(receivedPacket.getAddress(),receivedPacket.getPort()));
        		}
        		
            	if(message.getCode()==Message.ACK){
            		// add ACK message to ASCKSet and notify related thread
            		int repliedMessageId= message.getRepliedMessageId();
            		synchronized(ACKSet){
                		ACKSet.add(repliedMessageId);
            			ACKSet.notifyAll();
            		}
            	}
            	else if(message.getCode()==Message.REPLY){
                		// add reply message to replys and notify related thread
                		int repliedMessageId=message.getRepliedMessageId();
            			synchronized(replys){
                    		replys.put(repliedMessageId, message);
            				replys.notifyAll();
            			}
                }
                else
                	reactor.dispatch(message);
			}
        	catch(EOFException e){
        	}
        	catch (IOException e) {
	            if(isStopped()) {
	                return;
	            }
			}
		}
	}
		
    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    /**
     * Stop all including UDP socket
     */
    public synchronized void stop(){
        this.isStopped = true;	        
        socket.close();
		executorService.shutdown();
		reactor.shutdown();
		
        // stop log
        MessageLog.stop();
    }
 
    /**
     * Send reliable message to remote side
     * @param message       message to be sent
     * @param receiver      receiver endpoint
     * @param config        reliable configuration
     * @param replyHandler  object to handle reply 
     * @return              result after sending the message
     */
    public Result sendReliableMessage(Message message, Endpoint receiver, ReliableConfiguration config, Handler replyHandler){
		Future<Result> future =executorService.submit(new Callable<Result>(){
			public Result call() throws Exception{
				try{
					// set the message reliable
					message.setReliable();
					
					int messageId=message.getId();
					
					byte tempBuffer[]=Serializer.write(message);					
					
					// send the message
					DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, receiver.getAddress(), receiver.getPort());		    				    		
					
           		 	// log sent messages
           		 	MessageLog.info(MessageLog.SENT, message);
           		 	
					socket.send(sendPacket);	
										
					// wait for ACK message
					synchronized(ACKSet){					
					    int resendNum=config!=null?config.resendNum:defaultConfig.resendNum;
					    int ACKTimeout=config!=null?config.ACKTimeout:defaultConfig.ACKTimeout;
					    int replayTimeout=config!=null?config.replyTimeout:defaultConfig.replyTimeout;
					    
					    long startTime = System.currentTimeMillis();
					    long endTime;

					    while(!ACKSet.contains(messageId)){
					    	ACKSet.wait(ACKTimeout);			    	
							endTime = System.currentTimeMillis();
							if((endTime-startTime)>ACKTimeout){					
								if(resendNum>0){
									socket.send(sendPacket);
									resendNum--;
									startTime = System.currentTimeMillis();
								}
								else{
									return new Result(Result.TIMEOUT);
								}
							}
					    }	
					    
					    if(replyHandler!=null){					    
					    	// wait for reply
					    	synchronized(replys){
							    startTime = System.currentTimeMillis();
					    		while(!replys.containsKey(messageId)){			    			
					    			replys.wait(replayTimeout);				
					    			endTime = System.currentTimeMillis();
									if((endTime-startTime)>replayTimeout){
										return new Result(Result.TIMEOUT);
									}
					    		}
								return new Result(Result.REPLIED,replyHandler.handle(replys.get(messageId)));
					    	}
					    }
					    else
							return new Result(Result.RECEIVED);
					}			
				}
				catch(Exception e){
					return new Result(Result.EXTRA_ERROR);
				}
			}
		});
		try {
			return future.get();	// return result
		} catch (Exception e) {
			return new Result(Result.EXTRA_ERROR);
		}
	}
    
    /**
     * Send unreliable message to remote side
     * @param message       message to be sent
     * @param receiver      receiver endpoint
     */
    public void sendMessage(Message message,Endpoint receiver){
    	executorService.execute(new Runnable(){
    	    public void run() {
              	 try{	    				    		
            		 //byte tempBuffer[]=Serialization.serialize(message);
            		 byte tempBuffer[]=Serializer.write(message);
 					 
            		 DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, receiver.getAddress(), receiver.getPort());		    				    		
            		 
            		 // log sent messages
            		 MessageLog.info(MessageLog.SENT, message);
            		 
            		 socket.send(sendPacket);	 
            		             		 
              	 }
               	 catch(Exception e){
               		 //e.printStackTrace();
               		 return;
               	 }    	    
           }
    	});	
	}
    
    /**
     * Send PING message to endpoints to keep alive
     * @param timeout frequency to send message
     */
    public void keepAlive(int timeout){
    	Message message=new Message(userId,Message.PING,-1,null);
    	executorService.execute(new Runnable(){
    	    public void run() {
              	 try{	    				    		
              		 //byte tempBuffer[]=Serialization.serialize(message);
            		 byte tempBuffer[]=Serializer.write(message);
              		 while(!isStopped()){
              			 synchronized(keepAliveEndpoints){
              				 keepAliveEndpoints.forEach(endpoint->{
              					 try {
              						 DatagramPacket sendPacket = new DatagramPacket(tempBuffer, tempBuffer.length, endpoint.getAddress(), endpoint.getPort());
              						 socket.send(sendPacket);
              						 } catch (Exception e) {
    									//e.printStackTrace();
    								}
       	 						});
              				 keepAliveEndpoints.wait(timeout);
       	 					}
       	 				}
              	 }
               	 catch(Exception e){
               		 //e.printStackTrace();
               		 return;
               	 }    	    
           }
    	});	
    }
    
    /**
     * Test endpoint connected or not
     * @param remoter  remote user endpoint to be tested 
     * @return true if connected else false
     */
    public boolean testConnect(Endpoint remoter){
    	Message message=new Message(userId,Message.PING,-1,null);
    	ReliableConfiguration config=new ReliableConfiguration(100,0,2);
    	Result state=sendReliableMessage(message,remoter,config,null);
    	return state.getFlag()==Result.RECEIVED;
    }
    
    /**
     * Reliable configuration
     * @author Yifan Ruan (ry222ad@student.lnu.se)
     */
    class ReliableConfiguration{
    	int ACKTimeout;
    	int replyTimeout;
    	int resendNum;
    	
    	public ReliableConfiguration(int ACKTimeout, int replyTimeout,int resendNum){
    		this.ACKTimeout=ACKTimeout;
    		this.replyTimeout=replyTimeout;
    		this.resendNum=resendNum;
    	}
    }
    	
}
