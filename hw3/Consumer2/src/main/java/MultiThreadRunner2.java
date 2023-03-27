import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadRunner2 {
    private static AtomicInteger successReq;
    private static AtomicInteger failReq;
    private static final int numOfThread = 500;
    private static final int totReqPerTh = 2;
    private static Connection connection;
    private static ConcurrentHashMap<String, ArrayList<String>> record = new ConcurrentHashMap<>();
    private static String userId = "12345";

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {
        successReq = new AtomicInteger(0);
        failReq = new AtomicInteger(0);

        System.out.println("*********************************************************");
        System.out.println("Processing Begins");
        System.out.println("*********************************************************");


//        ConnectionFactory factory = new ConnectionFactory();
//        factory.setHost("localhost");
//        //factory.setHost("54.188.105.196");
//        connection = factory.newConnection();

        for (int i = 0; i < numOfThread; i++) {

            ConnectionFactory factory = new ConnectionFactory();
            //factory.setHost("localhost");
            factory.setHost("52.26.224.102");
            connection = factory.newConnection();

            Consumer2 consumer2 = new Consumer2(record,connection);
            Thread consumer2Thread = new Thread(consumer2);
            consumer2Thread.start();
        }
        System.out.println("finished thread looping");
        System.out.println("finished thread await");


        System.out.println("*********************************************************");
        System.out.println("Processing Ends");
        System.out.println("*********************************************************");
        System.out.println("Number of successful requests: " + successReq.get());
        System.out.println("Number of failed requests: " + failReq.get());
        System.out.println("list of liked users: " + record.get(userId));
    }
}
