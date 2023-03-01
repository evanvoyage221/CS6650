import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

public class MultiThreadRunner {
    private static final int numOfThread = 300;
    private static Connection connection;
    private static ConcurrentHashMap<String, ArrayList<Integer>> record = new ConcurrentHashMap<>();
    //private static String userId = "12345";
    private static CountDownLatch latch1;

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {

        System.out.println("*********************************************************");
        System.out.println("Processing Begins");
        System.out.println("*********************************************************");

        //latch1 = new CountDownLatch(numOfThread);

        ConnectionFactory factory = new ConnectionFactory();
        //factory.setHost("localhost");
        factory.setHost("35.91.156.178");
        connection = factory.newConnection();


        for (int i = 0; i < numOfThread; i++) {
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
