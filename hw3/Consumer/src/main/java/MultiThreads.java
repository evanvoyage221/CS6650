
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.util.concurrent.TimeoutException;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


public class MultiThreads {
  private static final int numOfThread = 10;
  private static Connection connection;
  public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {


    System.out.println("*********************************************************");
    System.out.println("Processing Begins");
    System.out.println("*********************************************************");

    ConnectionFactory factory = new ConnectionFactory();

    factory.setHost("localhost");

    connection = factory.newConnection();

//    AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
//    System.setProperty("aws.accessKeyId", "ASIA32MRRSJ3SUUV2AHJ");
//    System.setProperty("aws.secretAccessKey", "MFXoWoq4swr75jF8QcxBAhRUYoEcVxCo6Wk/jhNf");
//    System.setProperty("aws.sessionToken", "FwoGZXIvYXdzEA8aDIqK2TDEykj6Zz1bNSLJAUXOfGUgdOd0FrUk6oP7j8GVfS0CQODyzk2YHEy1dbESgOBlph26d3GRqdyeS803x6u+QbA2/obW0Cfb2EJ5DDFSiApsS/iWqMOm2IZJuF3hcmPqErZg+3bZu7JO8RHE5ShwBTqHudcjGE058ejy8ygioBk06rvZsk+FucAbEXOFXf1dkOTg6CNOk3e3Kje6Bz/vHSpEgAQs2Y4uyVjLi1aqgOB2VJGBPnHymBwqv8dWr8jJvFtqlcKVlcaNQkwE7nVYJkcyxwpZlyj5/fKgBjItARQwTMPYfg3kasaMWSHQvVbwIIZRvV1d9Zi9NKj6MBpb8Y2d/5FOU3XhKize");


//    DynamoDbClient client1 = DynamoDbClient.builder()
//        .credentialsProvider(credentialsProvider)
//        .region(Region.US_WEST_2)
//        .build();
//    DynamoDbClient client2 = DynamoDbClient.builder()
//        .credentialsProvider(credentialsProvider)
//        .region(Region.US_WEST_2)
//        .build();

//    DynamoDbClient client = DynamoDBService.getClient();
//    String createTable = DynamoDBService.createTable(client,"MyTable","SwipeID");

    DynamoDbClient client1 = DynamoDBService.getClient();
    DynamoDbClient client2 = DynamoDBService.getClient();

    for (int i = 0; i < numOfThread; i++) {
      Consumer1 consumer1 = new Consumer1(connection, client1, client2);
      Thread thread = new Thread(consumer1);
      thread.start();
      System.out.println("start  " + i);
    }
    System.out.println("finished thread looping");
    System.out.println("finished thread await");



    System.out.println("*********************************************************");
    System.out.println("Processing Ends");
    System.out.println("*********************************************************");




  }
}