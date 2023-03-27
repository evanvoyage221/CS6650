import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable{

    private static final String QUEUE_NAME = "TwinderConsumer1";
    private static ConcurrentHashMap<String, ArrayList<Integer>> record;
    private static Connection connection;
    private static final String TABLE_NAME = "MyTable2";
    private static final String SWIPE_ID = "SwipeID";
    private static final String NUM_OF_LIKES = "NumOfLikes";
    private static final String TOP100_LIKES = "Top100Likes";
    private DynamoDbClient client2 = DynamoDBService.getClient();

    public Consumer(ConcurrentHashMap<String, ArrayList<Integer>> record, Connection connection) {
        this.record = record;
        this.connection = connection;
    }

    @Override
    public void run(){
        Channel channel;

            try {
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                //channel.basicQos(1);
                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");


                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println(" [*] Received '" + message + "'" + "\n" + "Now start processing..");
                        processMsg(message);
                        System.out.println("[x] Done!");
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
                System.out.println("finishing basic consuming");
                }catch (IOException e) {
                e.printStackTrace();
            }

    }

    public void processMsg(String message){
        String[] msgParts = message.split("/");
        //Message msg = new Message(msgParts[0], msgParts[1], msgParts[2], msgParts[3]);
        if (msgParts[0].equals("left")){
            return;
        }
       DynamoDbClient client = DynamoDBService.getClient();
        Map<String,AttributeValue> returnedItems = DynamoDBService.getDynamoDBItem(client,TABLE_NAME,SWIPE_ID,msgParts[1]);
        if (returnedItems == null){
            List<String> newList = new ArrayList<>();
            newList.add(msgParts[2]);
            DynamoDBService.putItemInTable(client,TABLE_NAME,SWIPE_ID,msgParts[1],NUM_OF_LIKES,1,TOP100_LIKES,newList);
        }else{
            //update table
            Integer newLikes = Integer.valueOf(returnedItems.get(NUM_OF_LIKES).s()) + 1;
            DynamoDBService.updateTableItem(client,TABLE_NAME,SWIPE_ID,msgParts[1],NUM_OF_LIKES,String.valueOf(newLikes));
            //List<String> top100 = returnedItems.get(TOP100_LIKES).ss();
            List<String> top100 = new ArrayList<>(returnedItems.get(TOP100_LIKES).ss());
            System.out.println(top100);
            if (top100.size() < 100 && top100.contains(msgParts[2])){
                top100.add(msgParts[2]);
                DynamoDBService.updateTableItem(client,TABLE_NAME,SWIPE_ID,msgParts[1],TOP100_LIKES,top100);
            }
        }
        client.close();
        //UpdateUserIntoDatabase(msgParts[0],msgParts[1],msgParts[2]);
    }

    private void UpdateUserIntoDatabase(String leftOrRight, String swiper, String swipee
                                       ) {
        Map<String, AttributeValue> keyToFind = new HashMap<>();
        keyToFind.put("SwipeID", AttributeValue.builder().s(swiper).build());
        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME)
                .key(keyToFind)
                .build();
        GetItemResponse response = client2.getItem(request);
        if (response.hasItem()) {
            updateIntoTable2(response, leftOrRight, swipee, swiper);
        } else {
            writeIntoTable2(swiper, swipee, leftOrRight);
        }
    }

    private void writeIntoTable2(String swiper, String swipee,String leftOrRight) {
        int likes = 0;
        int dislikes = 0;
        List<String> person = new ArrayList<>();
        if (Objects.equals(leftOrRight, "left")){
            likes += 1;
            person.add(swipee);
        } else {
            dislikes += 1;
        }
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(SWIPE_ID, AttributeValue.builder().s(swiper).build());
        item.put(NUM_OF_LIKES, AttributeValue.builder().n(String.valueOf(likes)).build());
        item.put("dislikes", AttributeValue.builder().n(String.valueOf(dislikes)).build());
        Gson gson = new Gson();
        try {
            String personJson = gson.toJson(person);
            item.put(TOP100_LIKES, AttributeValue.builder().s(personJson).build());
        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(item)
                .build();
        PutItemResponse putItemResponse = client2.putItem(putItemRequest);
        if (putItemResponse.sdkHttpResponse().isSuccessful()) {
            System.out.println("Item added to table");
        } else {
            System.err.println("Failed to add item to table");
        }
    }
    private void updateIntoTable2(GetItemResponse response, String leftOrRight, String swipee, String swiper) {
        Map<String, AttributeValue> item = response.item();
        Map<String, AttributeValue> newItem = new HashMap<>();
        System.out.println(item);
        int likes = Integer.parseInt(item.get("likes").n());
        int dislikes = Integer.parseInt(item.get("dislikes").n());
        ArrayList person = new Gson().fromJson(item.get("person").s(), ArrayList.class);
        if (Objects.equals(leftOrRight, "left")){
            likes += 1;
            person.add(swipee);
        } else {
            dislikes += 1;
        }
        newItem.put(SWIPE_ID, AttributeValue.builder().s(swiper).build());

        newItem.put(NUM_OF_LIKES, AttributeValue.builder().n(String.valueOf(likes)).build());
        newItem.put("dislikes", AttributeValue.builder().n(String.valueOf(dislikes)).build());
        Gson gson = new Gson();
        try {
            String personJson = gson.toJson(person);
            newItem.put(TOP100_LIKES, AttributeValue.builder().s(personJson).build());
        } catch (JsonIOException e) {
            e.printStackTrace();
        }
        PutItemRequest putItemRequest = PutItemRequest.builder()
                .tableName(TABLE_NAME)
                .item(newItem)
                .build();
        PutItemResponse putItemResponse = client2.putItem(putItemRequest);
        if (putItemResponse.sdkHttpResponse().isSuccessful()) {
            System.out.println("Item added to table");
        } else {
            System.err.println("Failed to add item to table");
        }
    }

}
