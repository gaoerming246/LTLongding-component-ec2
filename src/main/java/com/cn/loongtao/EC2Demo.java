package com.cn.loongtao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AvailabilityZone;
import com.amazonaws.services.ec2.model.BlockDeviceMapping;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeAvailabilityZonesResult;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceStatusResult;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.EbsBlockDevice;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceNetworkInterfaceAttachment;
import com.amazonaws.services.ec2.model.InstanceStateName;
import com.amazonaws.services.ec2.model.InstanceStatus;
import com.amazonaws.services.ec2.model.Placement;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;
import com.amazonaws.services.ec2.model.Tag;
import com.amazonaws.services.opsworks.model.StartInstanceRequest;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;

/**
 *
 * @author lian
 * @version 1.0
 * @date 2016年3月4日下午5:02:06
 */

/* aws的案例 */
public class EC2Demo {

	public static AWSCredentials credentials = null;
	public static AmazonEC2 ec2Client = null;
	public static Region cnNorth = null;

	/**
	 * public Constructor
	 */
	static {
		try {
			credentials = new ProfileCredentialsProvider().getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
					+ "Please make sure that your credentials file is at the correct "
					+ "location (~/.aws/credentials), and is in valid format.", e);
		}
		ec2Client = new AmazonEC2Client(credentials);
		cnNorth = Region.getRegion(Regions.CN_NORTH_1);
		ec2Client.setRegion(cnNorth);
	}

	/**
	 * Initialize client
	 * 
	 * 
	 * @param client
	 *            Authenticated EC2 client
	 */
	public void InitEC2Operations(AmazonEC2 client) {
		// Initialize authorized client
		ec2Client = client;
	}

	/**
	 * Gather EC2 and format information for the passed instance 收集ec2，为案例格式化信息
	 * 
	 * @param instance
	 *            the instance whose info must be displayed
	 * @param buffer
	 *            a new StringBuffer
	 */
	private static void getInstanceInformation(Instance instance, StringBuffer buffer) {

		buffer.append(String.format("%n"));
		buffer.append(String.format("%nInstance Name:         %s%n", getInstanceName(instance)));
		buffer.append(String.format("Key Name:              %s%n", instance.getKeyName()));
		buffer.append(String.format("Instance ID:           %s%n", instance.getInstanceId()));
		buffer.append(String.format("Image ID:              %s%n", instance.getImageId()));
		buffer.append(String.format("Kernel ID:             %s%n", instance.getKernelId()));
		buffer.append(String.format("Instance Type:         %s%n", instance.getInstanceType()));
		buffer.append(String.format("Instance Architecture: %s%n", instance.getArchitecture()));
		buffer.append(String.format("Instance State:        %s%n", instance.getState().getName()));
		// buffer.append(String.format("Public DNS Name: %s%n",
		// instance.getPublicDnsName()));
		buffer.append(String.format("Hypervisor:            %s%n", instance.getHypervisor()));
		buffer.append(String.format("Owner:                 %s%n", getInstanceOwner(instance)));

	}

	/**
	 * get the owner of the instance 获取实例的归属者
	 * 
	 * @param instance
	 * @return
	 */
	private static Object getInstanceOwner(Instance instance) {
		for (Tag tag : instance.getTags()) {
			if (tag.getKey().equalsIgnoreCase("owner")) {
				return tag.getValue();
			}
		}
		return null;
	}

	/**
	 * get the name of the instance 获取实例的名称
	 * 
	 * @param instance
	 * @return
	 */
	private static Object getInstanceName(Instance instance) {
		for (Tag tag : instance.getTags()) {
			if (tag.getKey().equalsIgnoreCase("name")) {
				return tag.getValue();
			}
		}
		return null;
	}

	/**
	 * Display information for those instances associated with the specified key
	 * pair 展示通过指定keypair关联的实例信息
	 * 
	 * @param instances
	 *            A list of EC2 instances
	 */
	public static void displayInstancesInformation(List<Instance> instances) {

		StringBuffer buffer = new StringBuffer();
		for (Instance instance : instances)
			getInstanceInformation(instance, buffer);

		// display instance information
		System.out.println(buffer.toString());
	}

	/**
	 * create securityGroup 创建安全组
	 * 
	 * @param securityGroupName
	 * @param securityGroupDescription
	 */
	public void createSecuityGroup(String securityGroupName, String securityGroupDescription) {
		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest(securityGroupName,
				securityGroupDescription);
		CreateSecurityGroupResult createSecurityGroupResult = ec2Client.createSecurityGroup(createSecurityGroupRequest);
		System.out.println(String.format("Security group created: [%s]", createSecurityGroupResult.getGroupId()));
	}

	/**
	 * delete a securityGroup by name
	 * 
	 * @param securityGroupName
	 */
	public void deleteSecurityGroup(String securityGroupName) {
		DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest(securityGroupName);
		ec2Client.deleteSecurityGroup(deleteSecurityGroupRequest);
	}

	/**
	 * create a new ec2 instance 创建ec2实例
	 * 
	 * @param ami_id
	 *            The image Id (example, ami-e3106686)
	 * @param instanceType
	 *            The instance type (example, t1.micro)
	 * @param instanceNumber
	 *            The number of instances
	 * @param keyName
	 *            The name of the security key (example, Doc_Key)
	 * @param subnetId
	 *            The id of the subnet (example, subnet-827929e9)
	 * @param availZone
	 *            The availability zone (example, us-east-1d)
	 * @param ec2Client
	 *            The authorized EC2 client
	 */
	public void createInstance(AmazonEC2 ec2Client, String ami_id, String instanceType, Integer instanceNumber,
			String keyName, String subnetId, String availZone) {

		// the collection of instances
		List<Instance> instances = new ArrayList<Instance>();

		try {

			// Initialize the instance request
			RunInstancesRequest instancesRequest = new RunInstancesRequest();

			// Set the type of instance to create
			instancesRequest.setInstanceType(instanceType);

			// Sete the image Id
			instancesRequest.setImageId(ami_id);

			// Set max and min instances
			instancesRequest.setMaxCount(instanceNumber);
			instancesRequest.setMinCount(instanceNumber);

			// Add the security group to the request
			ArrayList<String> securityGroups = new ArrayList<String>();
			instancesRequest.setSecurityGroupIds(securityGroups);
			instancesRequest.setKeyName(keyName);

			// Set the subnet Id
			instancesRequest.setSubnetId(subnetId);

			// *************************** Add the block device mapping
			// ************************//

			// Goal: Setup block device mappings to ensure that we will not
			// delete
			// the root partition on termination.

			// Create the block device mapping to describe the root partition.
			BlockDeviceMapping blockDeviceMapping = new BlockDeviceMapping();
			blockDeviceMapping.setDeviceName("/dev/sda1");

			// Set the delete on termination flag to true
			EbsBlockDevice ebs = new EbsBlockDevice();
			ebs.setDeleteOnTermination(Boolean.TRUE);
			ebs.setVolumeSize(3);
			blockDeviceMapping.setEbs(ebs);

			// Add the block device mapping to the block list
			ArrayList<BlockDeviceMapping> blocklist = new ArrayList<BlockDeviceMapping>();
			blocklist.add(blockDeviceMapping);

			// Set the block device mapping configuration in the launch
			// specifications
			instancesRequest.setBlockDeviceMappings(blocklist);

			// *************************** Add the availability zone
			// ************************//
			// Setup the availability zone to use. Note we could retrieve the
			// availability
			// zones using the ec2.describeAvailabilityZones() API. For this
			// demo we will just use
			// us-east-1b.
			Placement placement = new Placement(availZone);
			instancesRequest.setPlacement(placement);

			// Create the instance
			RunInstancesResult result = ec2Client.runInstances(instancesRequest);
			// Store the created instance
			instances.addAll(result.getReservation().getInstances());
		} catch (AmazonServiceException e) {
			// Write out any exceptions that may have occurred.
			System.out.println("Error cancelling instances");
			System.out.println("Caught Exception:" + e.getMessage());
			System.out.println("Response status Code:" + e.getStatusCode());
			System.out.println("Error Code" + e.getErrorCode());
			System.out.println("Request ID" + e.getRequestId());
		}

		// Display instance information
		StringBuffer buffer = new StringBuffer();

		for (Instance instance : instances) {
			getInstanceInformation(instance, buffer);
		}

		System.out.println(buffer.toString());
	}

	/**
	 * Get information for the instance with the specified Id 通过特殊的id获取实例的信息
	 * 
	 * @param instanceId
	 *            The Id of the instance
	 */
	public static void getInstanceInformation(String instanceId) {
		DescribeInstancesRequest describeInstancesRequest = new DescribeInstancesRequest();
		List<String> list = new ArrayList<String>();
		list.add(instanceId);
		describeInstancesRequest.setInstanceIds(list);

		DescribeInstancesResult result = ec2Client.describeInstances(describeInstancesRequest);
		List<Reservation> reservations = result.getReservations();

		int runningInstanceGroup = 0;
		int runningInstances = 0;
		StringBuffer buffer = new StringBuffer();
		for (Reservation reservation : reservations) {
			List<Instance> instances = reservation.getInstances();
			runningInstanceGroup++;
			runningInstances += instances.size();
			for (Instance instance : instances)
				getInstanceInformation(instance, buffer);

			buffer.append(String.format("Running Instance Groups:    %d%n", runningInstanceGroup));
			buffer.append(String.format("Running Instances:         %d%n", runningInstances));
		}

		System.out.println(buffer.toString());
	}

	/**
	 * Get available instances associated with a specific key pair Display
	 * related information
	 * 
	 * @param keyName
	 *            The key pair name associated with the instances
	 */
	public static void getInstancesInformation(String keyName) {
		List<Instance> resultList = new ArrayList<Instance>();
		DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances();
		List<Reservation> reservations = describeInstancesResult.getReservations();
		for (Iterator<Reservation> iterator = reservations.iterator(); iterator.hasNext();) {
			Reservation reservation = iterator.next();
			for (Instance instance : reservation.getInstances()) {
				if (instance.getKeyName().equals(keyName))
					resultList.add(instance);
			}
		}
		displayInstancesInformation(resultList);
	}

	/**
	 * List the availability zones in a region,and the instances running in
	 * those zones
	 * 
	 * 列出region的可用区及在可用区内的实例
	 */
	public static void getAvailabilityZones() {
		StringBuffer buffer = new StringBuffer();

		DescribeAvailabilityZonesResult availabilityZonesResult = ec2Client.describeAvailabilityZones();
		List<AvailabilityZone> availabilityZones = availabilityZonesResult.getAvailabilityZones();

		buffer.append(String.format("You have access to %d availability zones:%n", availabilityZones.size()));
		for (AvailabilityZone zone : availabilityZones)
			buffer.append(String.format(" - %s (%s) %n", zone.getZoneName(), zone.getRegionName()));

		DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances();
		Set<Instance> instances = new HashSet<Instance>();
		for (Reservation reservation : describeInstancesResult.getReservations()) {
			instances.addAll(reservation.getInstances());
		}

		buffer.append(String.format("%nYou have %d Amazon EC2 instance running", instances.size()));
		System.out.println(buffer.toString());
	}

	/**
	 * Set the instance name and owner tag attribute 设置实例的名称和归属者属性
	 * 
	 * @param instanceId
	 *            The Id of the instance
	 * @param name
	 *            The name to assign to the instance
	 * @param owner
	 *            The name of the owner
	 */
	public static void setInstanceAttributes(String instanceId, String name, String owner) {
		List<Instance> resultList = new ArrayList<Instance>();
		DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances();
		List<Reservation> reservations = describeInstancesResult.getReservations();

		// Obtain the instances with the specified Id
		for (Iterator<Reservation> iterator = reservations.iterator(); iterator.hasNext();) {
			Reservation reservation = iterator.next();
			for (Instance instance : reservation.getInstances()) {
				if (instance.getInstanceId().equals(instanceId)) {
					resultList.add(instance);
				}
			}
		}

		// Assign the names to the instances and the owner
		int idx = 0;
		for (Instance instance : resultList) {
			if (instance.getInstanceId().equals(instanceId)) {
				CreateTagsRequest createTagsRequest = new CreateTagsRequest();
				createTagsRequest.withResources(instance.getInstanceId()).withTags(new Tag("Name", name + idx))
						.withTags(new Tag("owner", owner));
				ec2Client.createTags(createTagsRequest);
			}
			idx++;
		}

		// Display instance isformation
		displayInstancesInformation(resultList);
	}

	/**
	 * start a instance by instanceName 启动一个实例
	 * 
	 * @param instanceName
	 *            The name of instance
	 */
	public static void startInstance(String instanceName) {

		DescribeInstancesResult describeInstancesResult = ec2Client.describeInstances();
		String startInstanceId = null;

		for (Reservation reservation : describeInstancesResult.getReservations()) {
			for (Instance instance : reservation.getInstances()) {
				for (Tag tag : instance.getTags()) {
					if (tag.getValue().equals(instanceName)) {
						startInstanceId = instance.getInstanceId();
					}
				}
			}
		}

		StartInstancesRequest startRequest = new StartInstancesRequest();
		startRequest.withInstanceIds(startInstanceId);

		StartInstancesResult result = ec2Client.startInstances(startRequest);

		boolean chkStatus = true;
		while (chkStatus) {
			DescribeInstancesResult descInstanceResult = ec2Client
					.describeInstances(new DescribeInstancesRequest().withInstanceIds(startInstanceId));
			Instance instance = descInstanceResult.getReservations().get(0).getInstances().get(0);
			String stateInstance = instance.getState().getName();

			if (InstanceStateName.Running.toString().equalsIgnoreCase(stateInstance)) {
				DescribeInstanceStatusResult descInstanceStatusResult = ec2Client.describeInstanceStatus(
						new DescribeInstanceStatusRequest().withInstanceIds(instance.getInstanceId()));
				InstanceStatus statuses = descInstanceStatusResult.getInstanceStatuses().get(0);
				String instanceCheck = statuses.getInstanceStatus().getStatus();
				String sysCheck = statuses.getSystemStatus().getStatus();

				if (instanceCheck.equals("ok") && sysCheck.equals("ok")) {
					chkStatus = false;
					System.out.println(instanceName + "开始了");
					break;
				}
			}
		}
	}

	/**
	 * stop a instance by instanceName 停止一个实例
	 * 
	 * @param instanceName
	 *            The name of instance
	 */
	public static void stopInstance(String instanceName) {
		DescribeInstancesResult descInst = ec2Client.describeInstances();

		String stopInstId = null;

		for (Reservation reservation : descInst.getReservations()) {
			for (Instance instance : reservation.getInstances()) {
				for (Tag tag : instance.getTags()) {
					if (tag.getValue().equals(instanceName)) {
						stopInstId = instance.getInstanceId();
					}
				}
			}
		}

		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();

		stopInstancesRequest.withInstanceIds(stopInstId);

		StopInstancesResult stopResult = ec2Client.stopInstances(stopInstancesRequest);
		System.out.println(stopInstId);

		boolean chkStatus = true;
		while (chkStatus) {
			DescribeInstancesResult describeInstancesResult = ec2Client
					.describeInstances(new DescribeInstancesRequest().withInstanceIds(stopInstId));
			String stateInstance = describeInstancesResult.getReservations().get(0).getInstances().get(0).getState()
					.getName();
			if (InstanceStateName.Stopped.toString().equalsIgnoreCase(stateInstance)) {
				System.out.println(instanceName + "停止了。");
				break;
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

		EC2Demo ec2Test = new EC2Demo();
		// ec2Test.createSecuityGroup("firstSecurityGroup","my first
		// securityGroup");
		// ec2Test.deleteSecurityGroup("firstSecurityGroup");
		stopInstance("ec2APITest");
	}
}
