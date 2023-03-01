import Model.Body;
import Model.ChannelPool;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "TwinderServlet", value = "/TwinderServlet")
public class TwinderServlet extends HttpServlet {
    //private static final String QUEUE_NAME = "Twinder";
    private ChannelPool channelPool;
    private Connection connection;
    private ConnectionFactory factory;
    String action;

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            this.channelPool = new ChannelPool();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
    }

    private boolean isUrlValid(String[] urlPath) {
        if (urlPath.length == 2) {
            return validShortUrl(urlPath);
        }
        return false;
    }

    private boolean validShortUrl(String[] urlParts) {
        if (urlParts[0].equals("")) {
            try {
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        PrintWriter writer = res.getWriter();

        //check if the URL is empty
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            writer.write("Missing parameter");
            writer.close();
            return;
        }

        String[] urlParts = urlPath.split("/");

        //Get the POST request body from HttpServletRequest
        BufferedReader bufferedReader = req.getReader();
        StringBuilder sb = new StringBuilder();
        String requestString;
        String line;
        while ( (line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        requestString = sb.toString();

        //invalid url
        if ((!urlParts[1].equals("left")&&!urlParts[1].equals("right"))|| !isUrlValid(urlParts) || !isRequestBodyValid(requestString)) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.write("Invalid request/payload");
        } else {
            //valid url
            Body input = processRequest(requestString);
            action = urlParts[1];
            //System.out.println(action);
            try {
                String msg = action + "/"+ input.getSwiper() + "/" + input.getSwipee() + "/" + input.getComment();
                boolean flag = channelPool.sendToQueue(msg);
                if (flag == true) {
                    res.setStatus(HttpServletResponse.SC_CREATED);
                    writer.write("successfully send message to queue");
                }
                else {
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writer.write("Fails to send msg to queue");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            //writer.write(input.getComment());
        }
        writer.close();
    }

    private boolean isRequestBodyValid(String requestString) {
        Gson gson = new Gson();
        try {
            Body info = gson.fromJson(requestString, Body.class);
            return info.getSwipee() != null && info.getSwiper()!=null && info.getComment() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        //return true;
    }

    protected Body processRequest(String requestString)
            throws ServletException, IOException {
        Gson gson = new Gson();
        Body reqBody = new Body();
        try {
            reqBody = (Body)gson.fromJson(requestString, Body.class);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return reqBody;
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
