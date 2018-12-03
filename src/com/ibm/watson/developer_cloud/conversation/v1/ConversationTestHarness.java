/*
 * Copyright 2017 IBM Corp. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.ibm.watson.developer_cloud.conversation.v1;

import com.ibm.watson.developer_cloud.assistant.v1.Assistant;
import com.ibm.watson.developer_cloud.assistant.v1.model.InputData;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageOptions;
import com.ibm.watson.developer_cloud.assistant.v1.model.MessageResponse;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ConversationTestHarness {

	// Delimiter used in CSV file
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "INPUT,EXPECTED OUTPUT,ACTUAL OUTPUT,STATUS";

	private static FileWriter fileWriter = null;
	private static BufferedReader fileReader = null;
	
	private static GetPropertyValues gp = new GetPropertyValues();

	public static void main(String[] args) throws Exception {
		try {
			// Create the file reader
			fileReader = new BufferedReader(new FileReader(gp.getPropValues("inputFile")));
			// Create the file writer
			fileWriter = new FileWriter(gp.getPropValues("outputFile"));
			//Write the CSV file header
			fileWriter.append(FILE_HEADER.toString());
			//Add a new line separator after the header
			fileWriter.append(NEW_LINE_SEPARATOR);	

			Assistant service = new Assistant("2018-02-16");

			// TIMEdotCom
//		  service.setUsernameAndPassword("dd170eab-704e-4cc9-9e15-ccdad27c3f0e", "iP6TCzm1ob4W"); 
//
//		  String workspaceId = "2a79cc6d-85f4-42e0-a5e6-52cdae40f355";          

			// Genting
//		  service.setUsernameAndPassword("54ea4e60-7ef9-4aec-ad09-08f9b3a5ba1a", "4t7fKz1dx1vE"); 
//
//		  String workspaceId = "0984cbd6-13f3-4dec-bf19-e90fd4bebe42";		  

			// AIA
			service.setUsernameAndPassword("dd170eab-704e-4cc9-9e15-ccdad27c3f0e", "iP6TCzm1ob4W");

			String workspaceId = "647fd281-4c17-46fc-aa08-d5cc2b65e986";

			String line = "";
			String inputString = "";
			String outputString = "";
			// Read the CSV file header to skip it
			fileReader.readLine();

			// Read the file line by line starting from the second line
			while ((line = fileReader.readLine()) != null) {
				// Get all tokens available in line
				String[] tokens = line.split(COMMA_DELIMITER);
				if (tokens.length > 0) {
					inputString = tokens[0];
					outputString = tokens[1];
					System.out.println("Input:" + inputString);
					System.out.println("Expected Output:" + outputString);

					InputData input = new InputData.Builder(inputString).build();

					MessageOptions options = new MessageOptions.Builder(workspaceId).input(input).build();

					MessageResponse response = service.message(options).execute();

					JSONParser parser = new JSONParser();
					System.out.println(response);

					Object obj = parser.parse(response.toString());
					getValue(inputString, outputString, obj);
				}
			}

			System.out.println("CSV file was created successfully !!!");

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

			try {
				fileWriter.flush();
				fileWriter.close();
				fileReader.close();
			} catch (IOException e) {
				System.out.println("Error while flushing/closing fileWriter !!!");
				e.printStackTrace();
			}
		}
	}

	public static void getValue(String input, String expectedOutput, Object obj) {
		// JSONParser parser = new JSONParser();
		Map map = null;

		try {
			JSONObject jsonObject = (JSONObject) obj;

			map = ((Map) jsonObject.get("output"));
			ArrayList<String> array = new ArrayList<String>();
			// iterating address Map
			Iterator<Map.Entry> itr1 = map.entrySet().iterator();

			while (itr1.hasNext()) {
				Map.Entry pair = itr1.next();
				// System.out.println(pair.getKey() + " : " + pair.getValue());
				if (pair.getKey().equals("text")) {
					array = (ArrayList) pair.getValue();
				}
			}

			int count = 0;
			// loop array
			if (array != null) {
				Iterator<String> iterator = array.iterator();
				while (iterator.hasNext()) {
					String output = iterator.next();
					output = output.replaceAll("(\\d+),.*", "$1");
					if (!output.contains("rephrasing") && !output.equals("")) {
						System.out.println("Output:" + output);

						fileWriter.append(appendDQ(input));
						fileWriter.append(COMMA_DELIMITER);
						fileWriter.append(appendDQ(expectedOutput));
						fileWriter.append(COMMA_DELIMITER);
						fileWriter.append(appendDQ(output));
						fileWriter.append(COMMA_DELIMITER);
						if (expectedOutput.equalsIgnoreCase(output)) {
							fileWriter.append("PASS");
						} else {
							fileWriter.append("FAIL");
						}
						fileWriter.append(NEW_LINE_SEPARATOR);

					}

					count++;

				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static String appendDQ(String str) {
		return "\"" + str + "\"";
	}

}