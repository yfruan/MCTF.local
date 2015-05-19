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

/**
 * Flag of payload in "STUN" message
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class STUNFlag{	
	public static final int REGISTER=0;     // register public and private endpoints
	public static final int GETINFO=1;   	// gets network information of a specific user
	public static final int UNREGISTER=2;   // unregister 
}
	