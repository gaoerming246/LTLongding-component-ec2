package com.cn.loongtao.exercise;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;



/***
 *Instantiate the EC2 authorized service client, initialize the operations and the UI classes.  
 *Before running the code, you need to set up your AWS security credentials. You can do this by creating a 
 *file named "credentials" at ~/.aws/ (C:\Users\USER_NAME.aws\ for Windows users) and saving the following lines in 
 *the file:
 *<pre>
 *[default]
 *  aws_access_key_id = your access key id
 *  aws_secret_access_key = your secret key
 *</pre>
 *For more information, see <a href="http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html" target="_blank">Providing AWS Credentials in the AWS SDK for Java</a> 
 *and <a href="https://console.aws.amazon.com/iam/home?#security_credential" target="_blank">Welcome to Identity and Access Management</a>.
 *
 *<b>WARNING</b>: To avoid accidental leakage of your credentials, DO NOT keep the credentials file in your source directory.
 * @author Michael Miele
 */
public class Main {

	/**
	 * Instantiate the EC2 client, initialize the operation classes. 
	 * Instantiate the SimpleUI class to display the selection menu and process the user's input. 
	 * @see SimpleUI#SimpleUI(AmazonEC2)
	 * @param args; 
	 *  args[0] = Your name
	 *  args[1] = Greeting message
	 * 
	 */
	public static void main(String[] args) {

		// Client to authenticate and use to perform EC2 operations.
		AmazonEC2 ec2Client = null; 
		
		
		String name = null, topic = null;
		
		// Read input parameters.
		try {
				name = args[0];
				topic = args[1];
		}
		catch (Exception e) {
			System.out.println("IO error trying to read application input! Assigning default values.");
			// Assign default values if none are passed.
			if (args.length==0) {
				name = "User";
				topic = "AWS EC2 client console application";
			}
			else {
				System.out.println("IO error trying to read application input!");
				System.exit(1); 
			}
		}
		
		// Print greeting message.
		String startGreetings = String.format("Hello %s let's start %s", name, topic);
		System.out.println(startGreetings);
		

        //============================================================================================//
        //=============================== Create authenticate EC2 client =============================//
        //============================================================================================//

 
		try {
        	// Obtain authenticated EC2 client.
			ec2Client = ClientAuthentication.getAuthorizedEc2Client();
			// Set region.
			Region usEast1 = Region.getRegion(Regions.CN_NORTH_1);
	        ClientAuthentication.setEc2Region(usEast1);
		} 
        catch (AmazonServiceException ase) {
        	StringBuffer err = new StringBuffer();
        	
        	err.append(("Caught an AmazonServiceException, which means your request made it "
                      + "to Amazon EC2, but was rejected with an error response for some reason."));
       	   	err.append(String.format("%n Error Message:  %s %n", ase.getMessage()));
       	   	err.append(String.format(" HTTP Status Code: %s %n", ase.getStatusCode()));
       	   	err.append(String.format(" AWS Error Code: %s %n", ase.getErrorCode()));
       	   	err.append(String.format(" Error Type: %s %n", ase.getErrorType()));
       	   	err.append(String.format(" Request ID: %s %n", ase.getRequestId()));
        	
       	   	System.out.println(err.toString());
    	} 
		catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with EC2 , "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
		}
        
        
        if (ec2Client != null) {
			
			// Initialize the EC2Operations class to handle EC2 REST API calls.
        	EC2Operations.InitEC2Operations(ec2Client);
			
			// Instantiate the SimpleUI class and display menu.
			SimpleUI sui = new SimpleUI(ec2Client);
	
			// Start processing user's input.
			sui.processUserInput();
		}
		else 
			String.format("Error %s", "Main: authorized EC2 client object is null.");

	}
}
