package model;

import java.util.HashMap;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator;
import com.amazonaws.services.dynamodbv2.model.Condition;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.sns.model.DeleteEndpointRequest;

public class DeviceTable {

	private AmazonDynamoDB client;
	private DynamoDB dynamoDB;
	private String tableName;

	public DeviceTable(String region, String tableName) {
		// create a new DynamoDB client
		client= AmazonDynamoDBClient.builder().withRegion(region).withCredentials(new ProfileCredentialsProvider())
				.build();
		dynamoDB = new DynamoDB(client);
		this.tableName = tableName;
	}

	public ScanResult getUserDevices(String server, String user) {
		// Get registration entries for this user
		HashMap<String, Condition> scanFilter = new HashMap<String, Condition>();
		Condition conditionServer = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(server));
		Condition conditionUser = new Condition().withComparisonOperator(ComparisonOperator.EQ.toString())
				.withAttributeValueList(new AttributeValue().withS(user));
		scanFilter.put("smapServer", conditionServer);
		scanFilter.put("userIdent", conditionUser);
		ScanRequest scanRequest = new ScanRequest(tableName).withScanFilter(scanFilter);
		ScanResult scanResult = client.scan(scanRequest);

		return scanResult;
	}
	
	/*
	 * Delete the token 
	 */
	public void deleteToken(String token) {
		
		// Delete the obsolete token
		Table table = dynamoDB.getTable(tableName);	
		DeleteItemSpec deleteItemSpec = new DeleteItemSpec().withPrimaryKey(new PrimaryKey("registrationId", token));	 
		table.deleteItem(deleteItemSpec);
	}
}
