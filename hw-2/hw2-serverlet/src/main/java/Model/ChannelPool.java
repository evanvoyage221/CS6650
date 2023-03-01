package Model;

import com.mysql.cj.x.protobuf.Mysqlx;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeoutException;


public class ChannelPool {
    private ObjectPool<Channel> channelPool;
    private Connection connection;

    public ChannelPool() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        //connectionFactory.setHost("localhost");
        connectionFactory.setHost("35.91.156.178");

        try{
            this.connection = connectionFactory.newConnection();
        }catch (IOException | TimeoutException e) {
            System.err.println("failed to create connection");
            e.printStackTrace();
        }
        this.channelPool = new GenericObjectPool<Channel>(new ChannelPoolFactory());
    }

    public boolean sendToQueue(String msg) throws Exception{
        UUID idKey = UUID.randomUUID();//idempotency key
        try{
            Channel channel = channelPool.borrowObject();
            channel.queueDeclare("TwinderConsumer1",true,false,false,null);
            channel.queueDeclare("TwinderConsumer2",true,false,false,null);
            channel.basicPublish("","TwinderConsumer1",null,msg.getBytes(StandardCharsets.UTF_8));
            channel.basicPublish("","TwinderConsumer2",null,msg.getBytes(StandardCharsets.UTF_8));
            channelPool.returnObject(channel);
            return true;
        }catch (Exception e){
            return false;
        }
    }

    class ChannelPoolFactory extends BasePooledObjectFactory<Channel> {
        @Override
        public Channel create() throws IOException{
            return connection.createChannel();
        }

        @Override
        public PooledObject<Channel> wrap(Channel channel) {
            return new DefaultPooledObject<Channel>(channel);
        }
    }
}
