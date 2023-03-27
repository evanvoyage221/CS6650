import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

public class Consumer1 implements Runnable {

  private final static String QUEUE_NAME = "TwinderConsumer2";
  private Connection connection;
  private final static String table1Name = "MyTable";
  private final static String table2Name = "LikesAndDislikesTable";
  private DynamoDbClient client1;
  private DynamoDbClient client2;

  public Consumer1(Connection connection, DynamoDbClient client1, DynamoDbClient client2) {
    this.connection = connection;
    this.client1 = client1;
    this.client2 = client2;

  }

  @Override
  public void run() {
    Channel channel;

    try {

      channel = connection.createChannel();
      channel.queueDeclare(QUEUE_NAME, true, false, false, null);
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {

        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        String[] messageParts = message.split("/");
        String swiper = messageParts[1];
        String swipee = messageParts[2];
        String action = messageParts[0];
        String comment = messageParts[3];
        //writeSwipeEventIntoDatabase(action, swiper, swipee, comment);
        UpdateUserIntoDatabase(action, swiper, swipee, comment);
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      };
      channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
      });
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void writeSwipeEventIntoDatabase(String leftOrRight, String swiper, String swipee,
      String comment) {
    Map<String, AttributeValue> itemValues = new HashMap<>();
    String swipeID = String.valueOf(System.currentTimeMillis());
    itemValues.put("SwipeID", AttributeValue.builder().s(swipeID).build());
    itemValues.put("Action", AttributeValue.builder().s(leftOrRight).build());
    itemValues.put("SwiperID", AttributeValue.builder().s(swiper).build());
    itemValues.put("SwipeeID", AttributeValue.builder().s(swipee).build());
    itemValues.put("Comment", AttributeValue.builder().s(comment).build());
    PutItemRequest request = PutItemRequest.builder().tableName(table1Name).item(itemValues)
        .build();
    client1.putItem(request);
  }



  private void UpdateUserIntoDatabase(String leftOrRight, String swiper, String swipee,
      String comment) {
    Map<String, AttributeValue> keyToFind = new HashMap<>();
    keyToFind.put("SwipeID", AttributeValue.builder().s(swiper).build());
    GetItemRequest request = GetItemRequest.builder()
        .tableName(table2Name)
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
    item.put("swiper", AttributeValue.builder().s(swiper).build());
    item.put("likes", AttributeValue.builder().n(String.valueOf(likes)).build());
    item.put("dislikes", AttributeValue.builder().n(String.valueOf(dislikes)).build());
    Gson gson = new Gson();
    try {
      String personJson = gson.toJson(person);
      item.put("person", AttributeValue.builder().s(personJson).build());
    } catch (JsonIOException e) {
      e.printStackTrace();
    }
    PutItemRequest putItemRequest = PutItemRequest.builder()
        .tableName(table2Name)
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
    newItem.put("swiper", AttributeValue.builder().s(swiper).build());

    newItem.put("likes", AttributeValue.builder().n(String.valueOf(likes)).build());
    newItem.put("dislikes", AttributeValue.builder().n(String.valueOf(dislikes)).build());
    Gson gson = new Gson();
    try {
      String personJson = gson.toJson(person);
      newItem.put("person", AttributeValue.builder().s(personJson).build());
    } catch (JsonIOException e) {
      e.printStackTrace();
    }
    PutItemRequest putItemRequest = PutItemRequest.builder()
        .tableName(table2Name)
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



