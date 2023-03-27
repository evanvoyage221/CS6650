import Model.SwipeAction;
import Model.SwipeActionGenerator;
import Model.SwiperProcessor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadClient {
    private static String urlBase;
    private static AtomicInteger successReq;
    private static AtomicInteger failReq;
    private static BlockingQueue<SwipeAction> actions;
    private static final int numOfThread = 250;
    private static final int totReqPerTh = 2000;
    private static final int totalReq = 500000;

    public static void main(String[] args) throws InterruptedException {
        //urlBase = "http://localhost:8080/hw1_war_exploded";
        //urlBase = "http://35.162.232.94:8080/hw1_war";
        urlBase = "http://lb-sevlet-1252734141.us-west-2.elb.amazonaws.com/hw1_war";
        //urlBase = "http://MyLB-2075889409.us-west-2.elb.amazonaws.com/Server_war";

        successReq = new AtomicInteger(0);
        failReq = new AtomicInteger(0);
        actions = new LinkedBlockingQueue<>();

        System.out.println("*********************************************************");
        System.out.println("Processing Begins");
        System.out.println("*********************************************************");

        CountDownLatch latch1 = new CountDownLatch(numOfThread);
        long start = System.currentTimeMillis();
        SwipeActionGenerator eventGenerator = new SwipeActionGenerator(actions, totalReq);
        Thread generatorThread = new Thread(eventGenerator);
        generatorThread.start();

        for (int i = 0; i < numOfThread; i++) {
            SwiperProcessor postProcessor = new SwiperProcessor(urlBase,successReq, failReq, totReqPerTh, actions, latch1);
            Thread thread = new Thread(postProcessor);
            thread.start();
        }
        latch1.await();

        long end = System.currentTimeMillis();
        long wallTime = end - start;


        System.out.println("*********************************************************");
        System.out.println("Processing Ends");
        System.out.println("*********************************************************");
        System.out.println("Number of successful requests: " + successReq.get());
        System.out.println("Number of failed requests: " + failReq.get());
        //System.out.println("Total wall time: " + wallTime);
        System.out.println("Throughput: " + (int)((successReq.get()) / (double)(wallTime / 1000)) + " requests/second");
        //System.out.println("Throughput: 4378 requests/second");
    }
}
