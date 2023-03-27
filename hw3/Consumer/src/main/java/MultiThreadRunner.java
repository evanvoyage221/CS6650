
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;


import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class MultiThreadRunner {
    private static final int numOfThread = 100;
    private static Connection connection;
    private static ConcurrentHashMap<String, ArrayList<Integer>> record = new ConcurrentHashMap<>();
    private static CountDownLatch latch1;

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {

        System.out.println("*********************************************************");
        System.out.println("Processing Begins");
        System.out.println("*********************************************************");

//        DynamoDbClient client = DynamoDBService.getClient();
//        String createTable = DynamoDBService.createTable(client,"MyTable2","SwipeID");


        for (int i = 0; i < numOfThread; i++) {

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            //factory.setHost("52.26.224.102");
            connection = factory.newConnection();

            Consumer consumer = new Consumer(record,connection);
            Thread consumer1Thread = new Thread(consumer);
            consumer1Thread.start();
        }
        System.out.println("finished thread looping");
        latch1.await();
        System.out.println("finished thread await");


        System.out.println("*********************************************************");
        System.out.println("Processing Ends");
        System.out.println("*********************************************************");
        //System.out.println("Number of likes/dislikes: " + record.get(userId).get(0));
    }


}
