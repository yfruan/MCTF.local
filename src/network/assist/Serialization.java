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
import java.io.Serializable;
import org.apache.commons.lang3.SerializationUtils;

/**
 * Convert between object and byte array
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class Serialization {
	
	/**
	 * Convert object to byte array
	 * @param obj the source object
	 * @return converted byte array
	 */
	public static byte[] serialize(Object obj){
		return SerializationUtils.serialize((Serializable) obj);
	}
	
	/**
	 * Convert byte array to object
	 * @param data  the source byte array data 
	 * @return  converted object
	 */
	public static Object deserialize(byte[] data){
		return SerializationUtils.deserialize(data);
	}

}
