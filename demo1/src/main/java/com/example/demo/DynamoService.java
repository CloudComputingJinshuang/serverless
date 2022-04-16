package com.example.demo;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service("dynamoService")
public class DynamoService {
    private Logger logger = LoggerFactory.getLogger(DynamoService.class);
    @Value("${aws.dynamodb.tableName}")
    private String tableName;

    private DynamoDbClient dynamoDbClient;
    @Autowired
    public void setDynamoDbClient(DynamoDbClient dbClient) {
        this.dynamoDbClient = dbClient;
    }

    public Map<String,AttributeValue> getDynamoDBItem(String key,String keyVal) {

        HashMap<String, AttributeValue> keyToGet = new HashMap<String,AttributeValue>();

        keyToGet.put(key, AttributeValue.builder()
                .s(keyVal).build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(this.tableName)
                .build();

        try {
            Map<String,AttributeValue> returnedItem = dynamoDbClient.getItem(request).item();

            if (returnedItem != null) {
                Set<String> keys = returnedItem.keySet();
                System.out.println("Amazon DynamoDB table attributes: \n");
                logger.info("Amazon DynamoDB table attributes: \n");
                for (String key1 : keys) {
                    System.out.format("%s: %s\n", key1, returnedItem.get(key1).toString());
                    logger.info("%s: %s\n", key1, returnedItem.get(key1).toString());
                }
                return returnedItem;
            } else {
                System.out.format("No item found with the key %s!\n", key);
                logger.error("No item found with the key %s!\n", key);
            }
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            logger.error(e.getMessage());
            System.exit(1);
        }
        return null;
    }
    public PutItemResponse putItemInTable(String key,
                                          String keyVal,
                                          String token,
                                          String tokenVal){

        HashMap<String,AttributeValue> itemValues = new HashMap<String,AttributeValue>();

        // Add all content to the table
        itemValues.put(key, AttributeValue.builder().s(keyVal).build());
        itemValues.put(token, AttributeValue.builder().s(tokenVal).build());
        long expire = System.currentTimeMillis() / 1000L + 300L;
        itemValues.put("expire", AttributeValue.builder().n(String.valueOf(expire)).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        try {
            PutItemResponse response = dynamoDbClient.putItem(request);
            System.out.println(tableName +" was successfully updated");
            logger.info(tableName +" was successfully updated");
            return response;
        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            logger.error("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            logger.error(e.getMessage());
            System.exit(1);
        }
        return null;
    }
}
