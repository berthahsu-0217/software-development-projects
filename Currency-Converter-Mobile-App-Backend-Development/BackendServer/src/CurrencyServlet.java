/*
 * @author Bertha Hsu
 * This script is used as model (as in MVC) for the web service.
 * It contains code to send HTTP requests to 3rd party API and manage its response.
 */

import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;


public class CurrencyServlet extends HttpServlet {

    //Handle HTTP GET requests
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String result = "";

        String path = request.getServletPath();

        //If linking to dashboard
        if(path.equals("/dashboard")){

            //Connect to MongoDB cluster
            DatabaseConnect db = new DatabaseConnect("CurrencyDatabase", "logs");
            //Retrieve operation analytics
            Map<String, String> map = db.getAnalysis();
            for (Map.Entry<String, String> entry: map.entrySet()){
                request.setAttribute(entry.getKey(), entry.getValue());
            }
            //Retrieve log data
            String logs = db.getLogs();
            request.setAttribute("logs", logs);
            //Display on dashboard.jsp
            this.getServletContext().getRequestDispatcher("/dashboard.jsp").include(request, response);

            return;

         //If requesting a conversion
        }else if(path.equals("/app")) {

            String currency0 = request.getParameter("currencyFrom");
            String currency1 = request.getParameter("currencyTo");

            //Connect to MongoDB
            DatabaseConnect dc1 = new DatabaseConnect("CurrencyDatabase", "logs");

            //Connect to 3rd API
            Long start = System.currentTimeMillis();
            HttpURLConnection conn;
            URL url = new URL("https://api.exchangeratesapi.io/latest?base=" + currency0);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            //Tell the server what format we want back
            conn.setRequestProperty("Accept", "text/plain");

            //Read response from API
            String output = "";
            //Things went well, so let's read the response
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            while ((output = br.readLine()) != null) {
                result += output;
            }
            conn.disconnect();

            JSONParser parser = new JSONParser();
            JSONObject obj0 = new JSONObject();
            try {
                obj0 = (JSONObject) parser.parse(result);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Long end = System.currentTimeMillis();

            //Retrieve only the currency rate requested
            Map<String, Double> rates = (Map) obj0.get("rates");
            Double rate = rates.get(currency1);

            //Construct log data
            if (rate == null) {
                //No variable name found in map
                response.setStatus(401);
                dc1.insertDocument(
                        new Document("timestamp", (Long) System.currentTimeMillis() / 1000L)
                                .append("currencyFrom", currency0)
                                .append("currencyTo", currency1)
                                .append("currency_rate_date", (String) obj0.get("date"))
                                .append("exchange_rate", -1.0)
                                .append("error_message", "no rate found.")
                                .append("latency", end-start));
                return;
            }

            //Make response into a JSON-formatted string
            JSONObject obj1 = new JSONObject();
            obj1.put("currency0",currency0);
            obj1.put("currency1", currency1);
            obj1.put("exchange_rate", rate);
            String reply = obj1.toJSONString();
            //Things went well so set the HTTP response code to 200 OK
            response.setStatus(200);
            //Tell the client the type of the response
            response.setContentType("text/plain;charset=UTF-8");

            //Return the value from a GET request
            PrintWriter out = response.getWriter();
            out.println(reply);

            //Write log to MongoDB
            dc1.insertDocument(
                    new Document("timestamp", (Long) System.currentTimeMillis() / 1000L)
                            .append("currencyFrom", currency0)
                            .append("currencyTo", currency1)
                            .append("currency_rate_date", (String) obj0.get("date"))
                            .append("exchange_rate", rate)
                            .append("error_message", "Success")
                            .append("latency", end-start));

        }
    }


}
