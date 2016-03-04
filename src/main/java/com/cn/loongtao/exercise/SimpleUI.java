package com.cn.loongtao.exercise;


import java.io.BufferedReader;
import java.io.IOException;

import com.amazonaws.services.ec2.AmazonEC2;

 
/*** 
 * Display a selection menu for the user. 
 * Process the user's input and call the proper method based on the user's selection.
 * Each method calls the related 
 * <a href="http://docs.aws.amazon.com/AWSEC2/latest/APIReference/making-api-requests.html#using-libraries" target="_blank">AWS EC2 API</a> for Java.
 * @author Michael Miele.
 *
 */
public class SimpleUI {
	
	private StringBuilder menu;
	private static final String newline = System.getProperty("line.separator");
	
	private String keyName;
	private String instanceId;
	private String instanceName; 
	private String instanceOwner;
	
	private AmazonEC2 ec2Client = null;
	
	/**
	 * Initialize the menu that allows the user to make the allowed choices.
	 * @param client The authenticated EC2 client.
	 */
	SimpleUI(AmazonEC2 client) {
		menu = new StringBuilder();
		menu.append(String.format("Select one of the following options:%n"));	
		menu.append(String.format("%s %s Create EC2 instance(s).", newline, "i1"));
		menu.append(String.format("%s %s Get info for the instances with specified key pair.", newline, "i2"));
		menu.append(String.format("%s %s Get info for the instance with specified Id.", newline, "i3"));
		menu.append(String.format("%s %s Get availability zones.", newline, "i4"));
		menu.append(String.format("%s %s Set instance attributes.", newline, "i5"));
		
		menu.append(String.format("%s %s Get instance info.", newline, "t"));
		
		menu.append(String.format("%s %s  Display menu.", newline, "m"));
		menu.append(String.format("%s %s  Exit application.", newline, "x"));
		menu.append(newline);		
		
	
		// Display menu.
		System.out.println(menu.toString());
		
		ec2Client = client;
	}
	
	/**
	 * Read user input.
	 */
	private static String readUserInput(String msg) {
		
		// Open standard input.
		BufferedReader br = new BufferedReader(new java.io.InputStreamReader(System.in));

		String selection = null;
		
		//  Read the selection from the command-line; need to use try/catch with the
		//  readLine() method
		try {
			if (msg == null)
				System.out.print("\n>>> ");
			else
				System.out.print("\n" + msg);
			selection = br.readLine();
		} catch (IOException e) {
			System.out.println("IO error trying to read your input!");
			System.out.println(String.format("%s", e.getMessage()));
			System.exit(1);
		}
		
		return selection;

	}
	/***
	 * Get user selection and call the related method.
	 * Loop indefinitely until the user exits the application.
	 */
	public void processUserInput() {
		
		String in = null;
		while (true) {
			
			// Get user input.
			String selection = readUserInput(null).toLowerCase();	
			
			try{
				// Exit the application.
				if ("x".equals(selection))
					break;
				else
					if ("m".equals(selection)) {
						// Display menu
						System.out.println(menu.toString());
						continue;
					}
					else 
						// Read the input string.
						in = selection.trim();
	
			}
			catch (Exception e){
				// System.out.println(e.toString());
				System.out.println(String.format("Input %s is not allowed%n", selection));
				continue;
			}
			
			// Select action to perform.
			switch(in) {
			
				case "i1": {
				
					try{
						// Create EC2 instance(s).
						// Get key name.
						do {
							keyName = readUserInput("Key name: ");	
						} while(keyName.isEmpty());
						EC2Operations.createInstance(ec2Client, 
								"ami-0220b23b", "t2.micro", 1, keyName, 
								"subnet-5379fb36", "cn-north-1a");
						
					}
					catch (Exception e){
						System.out.println(String.format("%s", e.getMessage()));
					}
					break;
				}
				
				case "i2": {
					try{
						// Get info for the instances with specified key pair.
						do {
							keyName = readUserInput("Key name: ");	
						} while(keyName.isEmpty());
						EC2Operations.getInstancesInformation(keyName);
					}
					catch (Exception e){
						System.out.println(String.format("%s", e.getMessage()));
					}
					break;
				}
				
				case "i3": {
					try{
						// Get info for the instance with specified Id.
						do {
							System.out.println(String.format("%s", "Get the instances info to obtain the Id. "));
							instanceId = readUserInput("Instance Id: ");	
						} while(instanceId.isEmpty());
						EC2Operations.getInstanceInformation(instanceId);
					}
					catch (Exception e){
						System.out.println(String.format("%s", e.getMessage()));
					}
					break;
				}
				
				
				case "i4": {
					try{
						// List the availability zone for the client.
						EC2Operations.getAvailabilityZones();
					}
					catch (Exception e){
						System.out.println(String.format("%s", e.getMessage()));
					}
					break;
				}
				
				case "i5": {
					try{
						// Set instance attributes.
						do {
							instanceId = readUserInput("Instance Id: ");	
							instanceName = readUserInput("Instance name: ");	
							instanceOwner = readUserInput("Instance owner: ");	
						} while(instanceId.isEmpty()|| instanceName.isEmpty() || instanceOwner.isEmpty());
						EC2Operations.setInstanceAttributes(instanceId, instanceName, instanceOwner);
					}
					catch (Exception e){
						System.out.println(String.format("%s", e.getMessage()));
					}
					break;
				}
					
				
				default: {
					System.out.println(String.format("%s is not allowed", selection));
					break;
				}
			}
					
		}
		SimpleUI.Exit();
		
	}
	
	private static void Exit() {
		System.out.println("Bye!\n");
		return;
	}
}
