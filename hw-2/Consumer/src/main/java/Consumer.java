import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import model.Message;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;


public class Consumer implements Runnable{

    private static final String QUEUE_NAME = "TwinderConsumer1";
    private static ConcurrentHashMap<String, ArrayList<Integer>> record;
    private static Connection connection;

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
                channel.basicQos(1);
                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println(" [*] Received '" + message + "'" + "\n" + "Now start processing..");
                        //processMsg(message);
                        System.out.println("[x] Done!");
                        //channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                };
                channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
                System.out.println("finishing basic consuming");
                }catch (IOException e) {
                e.printStackTrace();
            }

    }

    public void processMsg(String message){
        String[] msgParts = message.split("/");
        Message msg = new Message(msgParts[0], msgParts[1], msgParts[2], msgParts[3]);

        if (!record.containsKey(msg.getSwiper())) {
            record.put(msg.getSwiper(),new ArrayList<Integer>());
            record.get(msg.getSwiper()).add(0);
            record.get(msg.getSwiper()).add(0);
            System.out.println("user NOT in the map");
        }

        if (msg.getAction().equals("left")) {
            record.get(msg.getSwiper()).set(0,record.get(msg.getSwiper()).get(0)+1);
        } else {
            record.get(msg.getSwiper()).set(1,record.get(msg.getSwiper()).get(1)+1);
        }
    }

}
