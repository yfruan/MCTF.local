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
package network.address;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Combination of user address and port
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Endpoint implements Serializable,KryoSerializable{
	private static final long serialVersionUID = 1L;
	
	private InetAddress address;
	private int port;
	
	
	@Override
	public void read(Kryo kryo, Input input) {
		try {
			this.address=InetAddress.getByName(input.readString());
			this.port=input.readInt();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeString(address.getHostAddress());
		output.writeInt(port);
	}
	

	public Endpoint(){}
	
	/**
	 * Constructor
	 * @param address the address
	 * @param port the port
	 */
	public Endpoint(InetAddress address, int port) {
		this.address = address;
		this.port = port;
	}
	
	/**
	 * Get the address
	 * @return the address
	 */
	public InetAddress getAddress() {
		return address;
	}
	
	/**
	 * Get the port
	 * @return the port
	 */
	public int getPort() {
		return port;
	}
	
	@Override
	public int hashCode() {
		 return address.hashCode()+port;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Endpoint){
			Endpoint endpoint=(Endpoint) obj;
	    	if(endpoint.getAddress().equals(this.address) && endpoint.getPort()==this.port)
	    		return true;
	    }
	    return false;
	}
	
	@Override
	public String toString(){
		return address+" : "+port;
	}
	
}
