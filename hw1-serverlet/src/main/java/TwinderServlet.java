import Model.Body;
import com.google.gson.Gson;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "TwinderServlet", value = "/TwinderServlet")
public class TwinderServlet extends HttpServlet {
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
            writer.write("Invalid request");
        } else {
            //valid url
            res.setStatus(HttpServletResponse.SC_CREATED);
            Body input = processRequest(requestString);
            writer.write(input.getComment());
        }
        writer.close();
    }

    private boolean isRequestBodyValid(String requestString) {
        Gson gson = new Gson();
        try {
            gson.fromJson(requestString, Body.class);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
}
