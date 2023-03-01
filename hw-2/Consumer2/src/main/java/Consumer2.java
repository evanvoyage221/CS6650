import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Connection;
import model.Message;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class Consumer2 implements Runnable{

    private static final String QUEUE_NAME = "TwinderConsumer2";
    private static ConcurrentHashMap<String, ArrayList<String>> record = new ConcurrentHashMap<>();
    private static Connection connection;

    public Consumer2(ConcurrentHashMap<String, ArrayList<String>> record, Connection connection) {
        this.record = record;
        this.connection = connection;
    }

    @Override
    public void run() {
            Channel channel;
            try {
                channel = connection.createChannel();
                channel.queueDeclare(QUEUE_NAME, true, false, false, null);
                channel.basicQos(1);
                System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    System.out.println(" [*] Received '" + message + "'" + "\n" + "Now start processing..");
                    processMsg(message);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    System.out.println("sending back acknowledgement");
                };

                channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
                });
                System.out.println("finishing basic consuming");

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public synchronized void processMsg(String message){
        String[] msgParts = message.split("/");
        Message msg = new Message(msgParts[0], msgParts[1], msgParts[2], msgParts[3]);
        System.out.println("finishing parsing the msg");


        if (!record.containsKey(msg.getSwiper())) {
            record.put(msg.getSwiper(),new ArrayList<String>());
            System.out.println("user NOT in the map");
        }

        if (msg.getAction().equals("right")) {
            if(record.get(msg.getSwiper()).size() < 100){
                record.get(msg.getSwiper()).add(msg.getSwipee());
            }
            System.out.println(msg.getSwiper() + "plus one swipe right");
        }
    }

}

