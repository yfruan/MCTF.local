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
package log;

import java.util.Date;
import java.util.Random;

import network.protocol.Message;

/**
 * Responsible for logging messages
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class MessageLog{
	
	private static String logFile;
	private static FileManager manager;
	private static int count=0;
	
	public static final int SENT=0;
	public static final int RECEIVED=1;
	
	
	static{
		logFile="log"+Math.abs(new Random().nextInt())+".csv";
		manager=new FileManager(logFile);
		manager.setMode(FileManager.WRITING);
	}

	/**
	 * Log information
	 * Each log includes log id, timestamp, message id, message type, message event  
	 * @param type the message type, received or sent
	 * @param message the message
	 */
	public static void info(int type,Message message){
		manager.writeLine(count++, System.nanoTime()/1000000, message.getId(), type, message.getEvent());
	}
	
	/**
	 * Log information
	 * Each log includes log id, timestamp, message id, message type, message event  	 
	 * @param type  the message type, received or sent
	 * @param timestamp the timestamp
	 * @param message the message
	 */
	public static void info(int type,Long timestamp, Message message){
		manager.writeLine(count++, timestamp, message.getId(), type, message.getEvent());
	}
	
	/**
	 * Start logging
	 */
	public static void start(){
		manager.open();
	}
	
	/**
	 * Stop logging
	 */
	public static void stop(){
		manager.close();
	}
	
	public static void main(String[] args){
		 Date date= new Date();
		 System.out.println(date.getTime());
		 System.out.println(System.currentTimeMillis());
	}

}
