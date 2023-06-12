package edu.cmu.minhsuah;

/*
 * @author Bertha Hsu
 * This script is used to retrieve currency exchange rate from the web service.
 * It contains code to build another thread different from the UI thread to separate tasks.
 */

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import android.os.AsyncTask;
import org.json.*;

/*
 * This class provides capabilities to search for a exchange rate given two currencies.
 */
public class RetrieveCurrencyRate {

    MainActivity ma = null;

    /**
     * Method search takes two currencies and sends a HTTP get request to the web service
     * @param currency0 currency to be converted from
     * @param currency1 currency to be converted to
     * @param ma main activity reference
     */
    public void search(String currency0, String currency1, MainActivity ma) {
        System.out.println("RetrieveCurrencyRate.search");
        this.ma = ma;
        new AsyncCurrencySearch().execute(currency0, currency1);
    }

    /*
     * AsyncTask provides a simple way to use a thread separate from the UI thread in which to do network operations.
     * doInBackground is run in the helper thread.
     * onPostExecute is run in the UI thread, allowing for safe UI updates.
     */
    private class AsyncCurrencySearch extends AsyncTask<String, Void, Double> {

        protected Double doInBackground(String... urls) {
            System.out.println("Helper: doInBackground");
            return search(urls[0], urls[1]);
        }

        protected void onPostExecute(Double rate) {
            System.out.println("Helper: onPostExecute");
            ma.convert(rate);
        }

        /**
         * Method search sends HTTP requests given two currencies
         * @param currency0 currency to be converted from
         * @param currency1 currency to be converted to
         * @return
         */
        private Double search(String currency0, String currency1) {

            String response = "";
            HttpURLConnection conn;
            int status = 0;

            try {
                String currencies = "currencyFrom="+currency0+"&currencyTo="+currency1;
                //Pass the currencies on the URL line
                URL url = new URL("https://rocky-woodland-38270.herokuapp.com" + "/app?"+currencies);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                //Tell the server what format we want back
                conn.setRequestProperty("Accept", "text/plain");

                //Wait for response
                status = conn.getResponseCode();

                //If things went poorly, don't try to read any response, just return
                if (status != 200) {
                    // not using msg
                    System.out.println("error1");
                    return -1.0;
                }
                String output = "";
                //Things went well so let's read the response
                BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                while ((output = br.readLine()) != null) {
                    response += output;
                }
                conn.disconnect();

            }
            catch (MalformedURLException e) {
                System.out.println("error3");
                e.printStackTrace();
            }   catch (IOException e) {
                System.out.println("error4");
                e.printStackTrace();
            }

            try {
                //Retrieve the exchange rate from the JSON formatted response
                System.out.println(response);
                JSONObject obj = new JSONObject(response);
                Double rate = obj.getDouble("exchange_rate");
                System.out.println(rate);
                return rate;
            }catch (JSONException e){
                e.printStackTrace();
                return -1.0;
            }

        }

    }
}