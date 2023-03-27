import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

@WebServlet(name = "TwinderStatsServlet", value = "/TwinderStatsServlet")
public class TwinderStatsServlet extends HttpServlet {

    private String userId;
    private static final String TABLE_NAME = "MyTable2";
    private static final String SWIPE_ID = "SwipeID";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

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

        //invalid url
        if (urlParts.length != 2) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writer.write("Invalid request/payload");
        } else {
            userId = urlParts[1];
            DynamoDbClient client = DynamoDBService.getClient();
            Map<String, AttributeValue> returnedItems = DynamoDBService.getDynamoDBItem(client,TABLE_NAME,SWIPE_ID,userId);
            try{
                if (returnedItems != null){
                    res.setStatus(HttpServletResponse.SC_OK);
                    writer.write("successfully send message to queue");
                }else{
                    res.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    writer.write("Fails to send msg to queue");
                }
//                res.setStatus(HttpServletResponse.SC_OK);
//                writer.write("successfully send message to queue");
            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
