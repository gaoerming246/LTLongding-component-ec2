package com.cn.loongtao.exercise;
import java.io.File;

import com.amazonaws.AmazonClientException;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;


/***
 * Create Amazon EC2 authorized client.
 * For more informations, see <a href="http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/getting-started-signup.html" 
 * target="_blank">Sign Up for Amazon Web Services and Get AWS Credentials</a>.
 * For lots more information on using the AWS SDK for Java, including information on
 * high-level APIs and advanced features, check out the <a href="http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/welcome.html" 
 * target="_blank">AWS SDK for Java</a> Developer's Guide.
 * Stay up to date with new features in the AWS SDK for Java by following the 
 * <a href="https://java.awsblog.com" target="_blank">AWS Java Developer Blog</a>. 
 * @author Michael Miele
 */
public class ClientAuthentication {

	static AmazonEC2 ec2Client;
	
    /***
     * Create authorized client to access the service.
     * Before you run this code, you need to set up your AWS security credentials to connect to AWS. 
     * You can do this by creating a file named "credentials" at ~/.aws/ (C:\Users\USER_NAME.aws\ for Windows users) 
     * and saving the following lines in the file:
     * <pre>
     * [default]
     * aws_access_key_id = your access key id
     * aws_secret_access_key = your secret key
     * </pre>
     * @return ec2Client Authorized EC2 client.
     * @throws AmazonClientException Issued if credential error is encountered.
     */
    public static AmazonEC2 getAuthorizedEc2Client()  throws AmazonClientException {
      
    	AWSCredentialsProvider credentialsProvider;
    	AWSCredentials credentials;
    	 
    	File credentialsFile = null;
    	ec2Client = null;
        
    	  try {
    		  	credentialsProvider = new ProfileCredentialsProvider();
    		  	credentials = credentialsProvider.getCredentials();
     
          } catch (Exception e) {
          		credentialsFile = new File(System.getProperty("user.home"), ".aws/credentials");
          		throw new AmazonClientException(
                      "Cannot load the credentials from the credential profiles file. " +
                      "Please make sure that your credentials file is at the correct " +
                      "location " + credentialsFile.getAbsolutePath() + " and is in valid format.", e);
          }
     
        ec2Client = new AmazonEC2Client(credentials);
        return ec2Client;
    }

    public static void setEc2Region(Region regionValue){
    	Region currentRegion = regionValue;
        ec2Client.setRegion(currentRegion);
    }
}
