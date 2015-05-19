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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import network.protocol.Event;

public class MessageLogAnalysis {
	
	private Long minTime=new Long(-1);                       // minimal sent message timestamp
	private String logFile;
	private String resultFile;
	public Map<Integer,MsgGroup> logMsgs=new HashMap<>();   
	
	public static void main(String[] args){
		String logFile="log1.csv";
		String nextLogFile="log2.csv";
		String resultFile="result1.csv";
		String nextResultFile="result2.csv";

		
		MessageLogAnalysis analysis=new MessageLogAnalysis(logFile,resultFile);
		MessageLogAnalysis nextAnalysis=new MessageLogAnalysis(nextLogFile,nextResultFile);

		analysis.construct();
		nextAnalysis.construct();
		
		analysis.filter();
		nextAnalysis.filter();
		
		analysis.setMinTime();
		nextAnalysis.setMinTime();
		
		analysis.compare(nextAnalysis.logMsgs);
		nextAnalysis.compare(analysis.logMsgs);

		//System.out.println(logMsgs.size());
		//System.out.println(nextLogMsgs.size());
	}
	
	public MessageLogAnalysis(String logFile,String resultFile){
		this.logFile=logFile;
		this.resultFile=resultFile;
	}
	
	// log information including log id, timestamp, message id, message type, message event 
	public void construct(){
		FileManager manager=new FileManager(logFile);
		manager.setMode(FileManager.READING);
		manager.open();
		String line;
		MsgGroup group;
		while((line=manager.readLine())!=null){
			String[] strs=line.split(",");
			int event=Integer.parseInt(strs[4]);
			int messageId=Integer.parseInt(strs[2]);
			Long timestamp=Long.parseLong(strs[1]);
						
			if(logMsgs.containsKey(event)){
				group=logMsgs.get(event);
			}
			else{
				group=new MsgGroup();
				logMsgs.put(event, group);
			}
			group.putMsg(Integer.parseInt(strs[3]), messageId, timestamp);
		}
		manager.close();
	}
	
	public void filter(){
		for(int i=-1;i<=Event.CONNECT;i++){
			logMsgs.remove(i);
		}
	}
	
	public void setMinTime(){
		FileManager manager=new FileManager(logFile);
		manager.setMode(FileManager.READING);
		manager.open();
		String line;
		while((line=manager.readLine())!=null){
			String[] strs=line.split(",");
			int event=Integer.parseInt(strs[4]);
			Long timestamp=Long.parseLong(strs[1]);
						
			if(event>Event.CONNECT && Integer.parseInt(strs[3])==MessageLog.SENT){
				minTime=timestamp;
				break;
			}
		}
		System.out.println("min time: "+minTime);
		manager.close();
	}
	
	
	public void compare(Map<Integer,MsgGroup> nextLogMsgs){
		FileManager manager=new FileManager(resultFile);
		manager.setMode(FileManager.WRITING);
		manager.open();
		Iterator<Integer> iterator=logMsgs.keySet().iterator();
		int total;
		int lostNum;
		while(iterator.hasNext()){
			int event=iterator.next();
			manager.writeLine("event "+event);
			System.out.println("Message event "+event);
			MsgGroup logMsgGroup=logMsgs.get(event);
			MsgGroup nextLogMsgGroup=nextLogMsgs.get(event);
			
			Map<Integer,Long> sentMsgs=logMsgGroup.sentMsgs;
			Map<Integer,Long> receivedMags=nextLogMsgGroup.receivedMags;
			
			Iterator<Integer> iter=sentMsgs.keySet().iterator();
			total=0;
			lostNum=0;
			while(iter.hasNext()){
				int messageId=iter.next();
				total++;
				if(receivedMags.containsKey(messageId)){
					Long sentTime=sentMsgs.get(messageId);
					Long receivedTime=receivedMags.get(messageId);
					
					System.out.println("Sent message "+messageId+" : "+sentTime+" received message: "+receivedTime+" delay "+(receivedTime-sentTime));
					manager.writeLine(messageId, sentTime-minTime,receivedTime-sentTime);
				}
				else{
					System.out.println("Message "+messageId+"from time "+sentMsgs.get(messageId)+" lost!!!!!");
					lostNum++;
				}
			}
			manager.writeLine("Total num: "+total+" Lost num: "+lostNum+" rate: "+(lostNum/((double)total)));
		}
		manager.close();
	}
		
	
	public class MsgGroup{
		// sent message id with timestamp
		Map<Integer,Long> sentMsgs=new HashMap<>();
		// received message id with timestamp
		Map<Integer,Long> receivedMags=new HashMap<>();
		
		public void putMsg(int type, int messageId, Long timestamp){
			if(type==MessageLog.SENT)
				sentMsgs.put(messageId, timestamp);
			else
				receivedMags.put(messageId, timestamp);
		}
	}
}
