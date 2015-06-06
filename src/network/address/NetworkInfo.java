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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Combination of user public and private endpoints
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class NetworkInfo implements Serializable,KryoSerializable{
	private static final long serialVersionUID = 1L;
	
	private String userId;
	private Endpoint privateEndpoint;
	private Endpoint publicEndpoint;
	
	
	@Override
	public void read(Kryo kryo, Input input) {
		this.userId=input.readString();
		this.privateEndpoint=(Endpoint)kryo.readClassAndObject(input);
		this.publicEndpoint=(Endpoint)kryo.readClassAndObject(input);
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeString(userId);
		kryo.writeClassAndObject(output, privateEndpoint);
		kryo.writeClassAndObject(output, publicEndpoint);
	}
	
	public NetworkInfo(){}
	
	/**
	 * Constructor
	 * @param userId the user id
	 * @param privateEndpoint the private endpoint
	 * @param publicEndpoint the public endpoint
	 */
	public NetworkInfo(String userId, Endpoint privateEndpoint,
			Endpoint publicEndpoint) {
		this.userId = userId;
		this.privateEndpoint = privateEndpoint;
		this.publicEndpoint = publicEndpoint;
	}

	/**
	 * Get the user id
	 * @return the user id
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Get the private endpoint
	 * @return the private endpoint
	 */
	public Endpoint getPrivateEndpoint() {
		return privateEndpoint;
	}

	/**
	 * Get the public endpoint
	 * @return the public endpoint
	 */
	public Endpoint getPublicEndpoint() {
		return publicEndpoint;
	}
	
}
