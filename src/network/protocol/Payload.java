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

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Message payload
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Payload implements Serializable,KryoSerializable{
	private static final long serialVersionUID = 1L;
	
	private int flag;    
	private Object data;
	

	@Override
	public void read(Kryo kryo, Input input) {
		this.flag=input.readInt();
		this.data=kryo.readClassAndObject(input);
	}

	@Override
	public void write(Kryo kryo, Output output) {
		output.writeInt(flag);
		kryo.writeClassAndObject(output, data);
	}
		
	public Payload(){}
	
	/**
	 * Constructor
	 * @param flage  the flag, identifying how to deal with the data
	 * @param data the data
	 */
	public Payload(int flage,Object data){
		this.flag=flage;
		this.data=data;
	}
	
	/**
	 * Get the flag
	 * @return the flag
	 */
	public int getFlag() {
		return flag;
	}
	
	/**
	 * Set the flag
	 * @param flag the flag
	 */
	public void setFlag(int flag) {
		this.flag = flag;
	}

	/**
	 * Get the data
	 * @return the data
	 */
	public Object getData() {
		return data;
	}

}
