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
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import network.Handler;
import network.Result;
import network.RUDPImpl;
import network.address.Endpoint;
import network.address.NetworkInfo;
import network.assist.STUNServerClient;
import network.assist.Serializer;
//import network.assist.Serialization;
import network.assist.TURNServerClient;
import network.protocol.ConnectFlag;
import network.protocol.Event;
import network.protocol.Message;
import network.protocol.Payload;

/**
 * Controller for building connection and managing registered controllers
 * responsible for "CONNECT", "STUN" and "TURN" message
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class CentralController extends AbstractController{
	private String userId;
	private RUDPImpl mainRUDP;
	
	private InetAddress hostAddress;

	private Set<AbstractController> controllers=new HashSet<>();    // registered channel controllers
	
	private ScheduledExecutorService scheduler=Executors.newScheduledThreadPool(10);
	
	private Endpoint STUNServerEndpoint;
	private Endpoint TURNServerEndpoint;
	private STUNServerClient stunServerClient;
	private TURNServerClient turnServerClient;
		
	private String remoteUserId;
	private Endpoint remoteEndpoint;	
	
	private String[] remoteUserIds;
	private  Map<String,NetworkInfo> remoteNetworkInfos=new ConcurrentHashMap<>(); 
	
	private boolean isConnectEnd=true;          
	
	private Hook inEstablishHook;       // when receiving "CONNECT" message for establishing
	private Hook connectHook; 			// when connectivity is ready       
	private Hook inTerminateHook;      	// when receiving "CONNECT" message for terminating

	/**
	 * Constructor
	 * @param userId the user id, identifying who owns the controller
	 * @param hostPort the host port
	 * @param relayServerAddress the relay server IP address
	 * @param STUNServerPort the STUN server port
	 * @param TURNServerPort the TURN server port
	 */
	public CentralController(String userId,int hostPort, String relayServerAddress, int STUNServerPort, int TURNServerPort){
		mainRUDP=new RUDPImpl(userId,hostPort);
    	new Thread(mainRUDP).start();	
    	
		this.userId=userId;	
		this.hostAddress=getHostAddress();

		try {
			STUNServerEndpoint=new Endpoint(InetAddress.getByName(relayServerAddress),STUNServerPort);
			TURNServerEndpoint=new Endpoint(InetAddress.getByName(relayServerAddress),TURNServerPort);
			this.stunServerClient=new STUNServerClient(userId, mainRUDP,STUNServerEndpoint);
			this.turnServerClient=new TURNServerClient(userId, mainRUDP,TURNServerEndpoint);
			
			//System.out.println("Register user");
			this.stunServerClient.register(hostAddress, hostPort);
			
			mainRUDP.keepAlive(5000);
			mainRUDP.addKeepAliveEndpoint(TURNServerEndpoint);		
		} catch (Exception e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * Register channel controller
	 * @param controller controller to be managed
	 * @return  true if success
	 */
	public boolean registerController(AbstractController controller){
		if(controller==null){
			System.out.println("The controller has not been initialized");
			return false;
		}
		controller.configure(userId, mainRUDP);
		this.controllers.add(controller);
		return true;
	}
	
	/**
	 * Set the hook,which is executed when receiving the communication invitation
	 * @param hook the hook
	 */
	public void setInEstablishHook(Hook hook){
		this.inEstablishHook=hook;
	}
	
	/**
	 * Set the hook, which is executed when current connection is borken
	 * @param hook the hook
	 */
	public void setInTerminateHook(Hook hook){
		this.inTerminateHook=hook;
	}
	
	/**
	 * Set the hook, which is executed when accepting the communication invitation
	 * @param hook the hook
	 */
	public void setConnectHook(Hook hook){
		this.connectHook=hook;
	}
	
	
	/**
	 * Set remote user ids
	 * @param remoteUserIds the list of remote user id
	 */
	public void setRemoteUserIds(String[] remoteUserIds){
		this.remoteUserIds=remoteUserIds;
	}

	/**
	 * Check online user from Relay Server periodically
	 * @param onlineHook  hook to be executed when finding new online user
	 * @param offlineHook hook to be executed when online user offline
	 */
	public void regularCheckRegisteredUser(Hook onlineHook, Hook offlineHook){
		scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
            	try{
            		NetworkInfo[] infos=stunServerClient.getInfo(remoteUserIds);
            		if(infos!=null){            			
            			for(NetworkInfo info:infos){
            				String userId=info.getUserId();
            				if(info.getPublicEndpoint()!=null){
                				remoteNetworkInfos.put(userId, info);
                				if(!mainRUDP.testConnect(info.getPrivateEndpoint()))
                					mainRUDP.addKeepAliveEndpoint(info.getPublicEndpoint());
                				onlineHook.execute(userId);
            				}
            				else{
                				if(remoteNetworkInfos.containsKey(userId)){
                					NetworkInfo networkInfo=remoteNetworkInfos.get(userId);
                					remoteNetworkInfos.remove(userId);
                					mainRUDP.removeKeepAliveEndpoint(networkInfo.getPublicEndpoint());
                					offlineHook.execute(userId);
                				}
            				}	
            			}
            		}
            	}
            	catch(Exception e){
            		e.printStackTrace();
            	}
            }
        },0,10,TimeUnit.SECONDS);
	}
	
	/**
	 * Register central controller handler
	 */
	public void registerControllerHandler(){
		Handler handler=(message)->{
			try{				
				Payload payload= (Payload)Serializer.read(message.getPayload(),Payload.class);
				
				if(payload.getFlag()==ConnectFlag.ESTABLISH){			
					remoteUserId=message.getSenderId();		
					if(isConnectEnd){  // determine current status of connectivity
						if(testConnect(remoteUserId)){       // determine the communication model, P2P or C/S
							int messageId=message.getId();
							int verifyNum=(int)payload.getData();
							boolean accept = (boolean)inEstablishHook.execute(remoteUserId);  // execute hook and get the decision from end user 

							Message reply;
							if (accept)
								reply=new Message(userId,Message.REPLY,messageId,
										Serializer.write(new Payload(ConnectFlag.ESTABLISH, verifyNum+1)));
							else 
								reply=new Message(userId,Message.REPLY,messageId,
										Serializer.write(new Payload(ConnectFlag.ESTABLISH, -1)));
							
							Result result=mainRUDP.sendReliableMessage(reply, remoteEndpoint, null, null);   
							if(result.getFlag()==Result.RECEIVED && accept){
		        				isConnectEnd=false;
								connectHook.execute();
							}
						}
					}
				}
				
				else if(payload.getFlag()==ConnectFlag.TERMINATE){
					Message reply=new Message(userId,Message.REPLY,message.getId(),
							Serializer.write(new Payload(ConnectFlag.TERMINATE, (int)payload.getData()+1)));
    				Result result=mainRUDP.sendReliableMessage(reply, remoteEndpoint, null, null);
    				if(result.getFlag()==Result.RECEIVED){
        				isConnectEnd=true;
        				inTerminateHook.execute();
    				}
    			}
				else{
					System.out.println("Wrong CONNECT message!!!");
				}				
			}
	        catch(Exception e){
	        	e.printStackTrace();
	        }
			return null;
		};
		
		this.mainRUDP.getReactor().register(Event.CONNECT, handler);
	}

	/**
	 * Connect with remote user
	 * @param remoteUserId  remote user to be connected
	 * @return   true if success
	 */
	public boolean connect(String remoteUserId){
		if(this.testConnect(remoteUserId)){
			int varifyNum=Math.abs((new Random()).nextInt());		// generate random number for authentication
			Payload payload=new Payload(ConnectFlag.ESTABLISH,varifyNum);
			Message message=new Message(userId,Event.CONNECT,Serializer.write(payload));						
			Handler handler= (reply)->{
					return (int)((Payload)Serializer.read(reply.getPayload(),Payload.class)).getData()==(varifyNum+1);
			};
			Result result=mainRUDP.sendReliableMessage(message, remoteEndpoint, null, handler);
			if(result.getFlag()==Result.REPLIED && (boolean)result.getData()){
				isConnectEnd=false;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Terminate current connectivity
	 * @return true if success
	 */
	public boolean disconnect(){		
		int varifyNum=Math.abs((new Random()).nextInt());
		Payload payload=new Payload(ConnectFlag.TERMINATE,varifyNum);
		Message message=new Message(userId,Event.CONNECT,Serializer.write(payload));						
		Handler handler= (reply)->{
				return (int)((Payload)Serializer.read(reply.getPayload(),Payload.class)).getData()==(varifyNum+1);
		};
		Result result=mainRUDP.sendReliableMessage(message, remoteEndpoint, null, handler);	
		if(result.getFlag()==Result.REPLIED && (boolean)result.getData()){
			isConnectEnd=true;
			return true;
		}
		return false;
	}
	
	/**
	 * Test connectivity, determining remote endpoint to send message
	 * P2P model: connect with public or private endpoint of remote side
	 * C/S model: require relay Server to exchange messages
	 * 
	 * @param remoteUserId the remote user id
	 * @return  true if success 
	 */
	private boolean testConnect(String remoteUserId){
		NetworkInfo remoteNetworkInfo=stunServerClient.getInfo(remoteUserId);		
		if(remoteNetworkInfo!=null){
			Endpoint privateEndpoint=remoteNetworkInfo.getPrivateEndpoint();
			Endpoint publicEndpoint=remoteNetworkInfo.getPublicEndpoint();			
			//System.out.println("private endpoint "+privateEndpoint.toString());
			//System.out.println("public endpoint "+publicEndpoint.toString());			
			try{
				boolean testPrivateResult=mainRUDP.testConnect(privateEndpoint);
				boolean testPublicResult=mainRUDP.testConnect(publicEndpoint);
				
				System.out.println("Test private connection: "+testPrivateResult);
				System.out.println("Test public connection: "+testPublicResult);
						
				
				if(testPrivateResult==true)
					remoteEndpoint=privateEndpoint;
				else if(testPublicResult==true)
					remoteEndpoint=publicEndpoint;
				else{
					System.out.println("Cannot connect the remote user!!!!");
					System.out.println("Connect through relay server!");
					if(this.turnServerClient.relay(publicEndpoint))
						remoteEndpoint=TURNServerEndpoint;
				}
				
				/*
				if(this.turnServerClient.relay(publicEndpoint)){
					System.out.println("Connect through relay server!");
					remoteEndpoint=TURNServerEndpoint;
				}
				else{
					System.out.println("Cann't Connect through relay server!");
					return false;
				}*/
				
				controllers.forEach(controller->controller.setRemoteEndpoint(remoteEndpoint));
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * Close
	 */
	public void close(){
		turnServerClient.unrelay();
		stunServerClient.unregister();
		scheduler.shutdown();
		mainRUDP.stop();
	}
	
	 /**
	  * Get host address of this machine
	  * @return the host address
	  */
	 private InetAddress getHostAddress(){
		 try {
			 Enumeration<NetworkInterface> enumeration;
			 enumeration = NetworkInterface.getNetworkInterfaces();
			 while (enumeration.hasMoreElements()) {
				 NetworkInterface i = (NetworkInterface) enumeration.nextElement();
				 for (Enumeration<InetAddress> en = i.getInetAddresses(); en.hasMoreElements();) {
					 InetAddress addr = (InetAddress) en.nextElement();
					 if (!addr.isLoopbackAddress()) {
						 if (addr instanceof Inet4Address)
							 return addr;
					 }
				 }
			 }
		 } catch (SocketException e) {
			 e.printStackTrace();
		 }
		    return null;
	 }

}
