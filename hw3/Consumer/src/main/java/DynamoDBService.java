import org.jetbrains.annotations.Nullable;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.*;

public class DynamoDBService {

    public static DynamoDbClient getClient() {
        AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
        System.setProperty("aws.accessKeyId", "ASIAWFGGVZGOL3MBKNSY");
        System.setProperty("aws.secretAccessKey", "2CYtZdiFgge9oJRZE9e54bc/Ex+apCu8AFgFSjBd");
        System.setProperty("aws.sessionToken", "FwoGZXIvYXdzEA8aDH26VYQ9mQcWa8XeWiLNAWmdixkvHZiJU7bhwyY5RkOb1v2fXcs0dZ9jWclacDEyv9fN2pagsv6zXsU8EYTlU5emGXUS4irIuFuIXuEC86zwvlZnpT4IC+rg6cec0mUK5JdZc/EUImplYrZX9jVEiqFwUUlIs7dc+VHCMPUGfvtPVIcNfe1yytIaeAM4Vw2eF29KUd3YvHSBr9Ttd1Zx3be9gsjsLJlA9gIriQHKq8hqu3bLDVj6ggGwN92a1SrtaTOH5wCZ58bkpRN7vRyGlrlDAROBfkqaDD1AImwosY7zoAYyLRthc37rj8Gu5wnZVi3NMIIWNnUo8AumRfx5vr3abY6X/KX3S8338wirRDU4Xw==");

        Region region = Region.US_WEST_2;
        return DynamoDbClient.builder()
                .region(region)
                .build();
    }
    public static String createTable(DynamoDbClient ddb, String tableName, String key) {
        DynamoDbWaiter dbWaiter = ddb.waiter();
        CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder().attributeName(key).attributeType(ScalarAttributeType.S).build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName(key)
                        .keyType(KeyType.HASH)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(new Long(10))
                        .writeCapacityUnits(new Long(10))
                        .build())
                .tableName(tableName)
                .build();

        String newTable ="";
        try {
            CreateTableResponse response = ddb.createTable(request);
            DescribeTableRequest tableRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();

            // Wait until the Amazon DynamoDB table is created.
            WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
            newTable = response.tableDescription().tableName();
            return newTable;

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    @Nullable
    public static Map<String,AttributeValue> getDynamoDBItem(DynamoDbClient ddb,String tableName,String key,String keyVal ) throws DynamoDbException {

        HashMap<String,AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(key, AttributeValue.builder()
                .s(keyVal)
                .build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .build();


        boolean hasItem = ddb.getItem(request).hasItem();
        if (hasItem) {
            Map<String,AttributeValue> returnedItem = ddb.getItem(request).item();
            Set<String> keys = returnedItem.keySet();
            System.out.println("Got Amazon DynamoDB table attributes: \n");

//            for (String key1 : keys) {
//                System.out.format("%s: %s\n", key1, returnedItem.get(key1).toString());
//            }
//                System.out.println(returnedItem.get("Top100Likes").ss());
//                returnedItem.get("Top100Likes").ss();
            return returnedItem;
        } else {
            System.out.format("No item found with the key %s!\n", key);
            return null;
        }
    }

    public static void updateTableItem(DynamoDbClient ddb,
                                       String tableName,
                                       String key,
                                       String keyVal,
                                       String name,
                                       String updateVal){

        HashMap<String,AttributeValue> itemKey = new HashMap<>();
        itemKey.put(key, AttributeValue.builder()
                .s(keyVal)
                .build());

        HashMap<String,AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(name, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s(updateVal).build())
                .action(AttributeAction.PUT)
                .build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(itemKey)
                .attributeUpdates(updatedValues)
                .build();

        try {
            ddb.updateItem(request);
        } catch (ResourceNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("The Amazon DynamoDB table was updated!");
    }

    public static void updateTableItem(DynamoDbClient ddb,
                                       String tableName,
                                       String key,
                                       String keyVal,
                                       String name,
                                       List<String> updateVal){

        HashMap<String,AttributeValue> itemKey = new HashMap<>();
        itemKey.put(key, AttributeValue.builder()
                .s(keyVal)
                .build());

        HashMap<String,AttributeValueUpdate> updatedValues = new HashMap<>();
        updatedValues.put(name, AttributeValueUpdate.builder()
                .value(AttributeValue.builder().ss(updateVal).build())
                .action(AttributeAction.PUT)
                .build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(tableName)
                .key(itemKey)
                .attributeUpdates(updatedValues)
                .build();

        try {
            ddb.updateItem(request);
        } catch (ResourceNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("The Amazon DynamoDB table was updated!");
    }


    public static void putItemInTable(DynamoDbClient ddb,
                                      String tableName,
                                      String key,
                                      String userId,
                                      String likesColumn,
                                      Integer numOflikes,
                                      String top100Column,
                                      List<String> top100likes){

        HashMap<String,AttributeValue> itemValues = new HashMap<>();
        itemValues.put(key, AttributeValue.builder().s(userId).build());
        itemValues.put(likesColumn, AttributeValue.builder().n(String.valueOf(numOflikes)).build());
        itemValues.put(top100Column, AttributeValue.builder().ss(top100likes).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(itemValues)
                .build();

        try {
            PutItemResponse response = ddb.putItem(request);
            System.out.println(tableName +" was successfully updated. The request id is "+response.responseMetadata().requestId());

        } catch (ResourceNotFoundException e) {
            System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
            System.err.println("Be sure that it exists and that you've typed its name correctly!");
            System.exit(1);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }


}
