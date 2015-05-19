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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Client for HTTP requesting
 * @author Yifan Ruan (ry222ad@student.lnu.se)
 */
public class HTTPClient { 
	/**
	 * HTTP get method
	 * @param url   api address
	 * @param token information to verify user authorization 
	 * @return result the result value
	 */
	public static String get(String url,String token){
		try{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Authorization", "Bearer "+token);

			int responseCode = connection.getResponseCode();
			if(responseCode==200){
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
 
				while ((inputLine = reader.readLine()) != null) {
					response.append(inputLine);
				}
				reader.close();
 
				System.out.println(response.toString());
				return response.toString();
			}
			else
				return null;
		}catch(Exception e){
			return null;
		}
	}
 
	
	/**
	 * HTTP post method
	 * @param url api address
	 * @param parameters data to be post
	 * @param token  information to verify user authorization 
	 * @return result the result value
	 */
	public static String post(String url,String parameters,String token){
		try{
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			connection.setRequestMethod("POST");
			if(token!=null)
				connection.setRequestProperty("Authorization", "Bearer "+token);
			// Send post request
			connection.setDoOutput(true);
			DataOutputStream output = new DataOutputStream(connection.getOutputStream());
			output.writeBytes(parameters);
			output.flush();
			output.close();
 
			int responseCode = connection.getResponseCode(); 
			if(responseCode==200){
				BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
 
				while ((inputLine = reader.readLine()) != null) {
					response.append(inputLine);
				}
				reader.close();		
				return response.toString();
			}
			else
				return null;
		}catch(Exception e){
			return null;
		}
	}
}
