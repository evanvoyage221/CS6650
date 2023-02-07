import Model.StatisticsCalculator;
import Model.SwipeAction;
import Model.SwipeActionGenerator;
import Model.SwiperProcessor;
import com.opencsv.CSVWriter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadClient {
    private static String urlBase;
    private static AtomicInteger successReq;
    private static AtomicInteger failReq;
    private static BlockingQueue<SwipeAction> actions;
    private static List<String[]> records;
    private static final int numOfThread = 250;
    private static final int totReqPerTh = 2000;
    private static final int totalReq = 500000;

    public static void main(String[] args) throws InterruptedException {
        //urlBase = "http://localhost:8080/hw1_war_exploded";
        urlBase = "http://18.236.134.109:8080/hw1_war";
        successReq = new AtomicInteger(0);
        failReq = new AtomicInteger(0);
        actions = new LinkedBlockingQueue<>();
        records = new ArrayList<>();
        records.add(new String[]{"Start Time", "Request Type", "Latency", "Response Code"});

        System.out.println("*********************************************************");
        System.out.println("Processing Begins");
        System.out.println("*********************************************************");

        CountDownLatch latch1 = new CountDownLatch(numOfThread);
        long start = System.currentTimeMillis();
        SwipeActionGenerator eventGenerator = new SwipeActionGenerator(actions, totalReq);
        Thread generatorThread = new Thread(eventGenerator);
        generatorThread.start();

        for (int i = 0; i < numOfThread; i++) {
            SwiperProcessor postProcessor = new SwiperProcessor(urlBase,successReq, failReq, totReqPerTh, actions, latch1,records);
            Thread thread = new Thread(postProcessor);
            thread.start();
        }
        latch1.await();

        long end = System.currentTimeMillis();
        long wallTime = end - start;

        // print records to CSV
        try {
            FileWriter fileWriter = new FileWriter("/Users/evangeline/Desktop/6650/hw1-Client/src/main/java/CSV/MultiThreadTest.CSV");
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(records);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StatisticsCalculator statisticsCalculator = new StatisticsCalculator(records);
        double meanVal = statisticsCalculator.getMeanValue();
        double medianVal = statisticsCalculator.getMedianValue();
        double percent99Val = statisticsCalculator.get99PercentValue();
        double maxVal = statisticsCalculator.getMaxValue();
        double minVal = statisticsCalculator.getMinValue();

        System.out.println("*********************************************************");
        System.out.println("Processing Ends");
        System.out.println("*********************************************************");
        System.out.println("Number of successful requests: " + successReq.get());
        System.out.println("Number of failed requests: " + failReq.get());
        System.out.println("Total wall time: " + wallTime);
        System.out.println("The mean response time: " + meanVal);
        System.out.println("The median response time: " + medianVal);
        System.out.println("Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
        System.out.println("The p99 (99th percentile) response time: " + percent99Val);
        System.out.println("The max response time: " + maxVal);
        System.out.println("The min response time: " + minVal);
    }
}
