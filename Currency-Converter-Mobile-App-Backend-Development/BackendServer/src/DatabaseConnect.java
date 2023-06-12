/*
 * @author Bertha Hsu
 * This script is used to connect to MongoDB cluster0.
 */

import java.util.*;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import com.mongodb.client.MongoCursor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatabaseConnect {

    MongoClient client;
    MongoDatabase database;
    MongoCollection<Document> collection;

    /**
     * Constructor connects to a collection in a database stored in MongoDB
     * @param database_name database name
     * @param collection_name collection name
     */
    public DatabaseConnect(String database_name, String collection_name){
        client = MongoClients.create("mongodb+srv://minhsuah:0217@cluster0-3x6rr.mongodb.net/test?retryWrites=true&w=majority");
        database = client.getDatabase(database_name);
        collection = database.getCollection(collection_name);
    }

    /**
     * Method insertDocument inserts a document in the collection
     * @param doc document storing log data
     */
    public void insertDocument(Document doc){
        collection.insertOne(doc);
    }

    /**
     * Method getDocuments returns all documents stored in the collection
     * @return a list of documents
     */
    public ArrayList<Document> getDocuments(){
        ArrayList<Document> ret = new ArrayList<> ();
        MongoCursor<Document> cursor = collection.find().iterator();
        try {
            while (cursor.hasNext()) {
                ret.add(cursor.next());
            }
        } finally {
            cursor.close();
        }
        return ret;
    }

    /**
     * Method getLogs retrieves log data from the collection
     * @return JSON formatted string of logs
     */
    public String getLogs(){

        JSONObject obj = new JSONObject();
        ArrayList<Document> documents = this.getDocuments();

        JSONArray id_list = new JSONArray();
        JSONArray timestamp_list = new JSONArray();
        JSONArray currencyFrom_list = new JSONArray();
        JSONArray currencyTo_list = new JSONArray();
        JSONArray currency_rate_date_list = new JSONArray();
        JSONArray exchange_rate_list= new JSONArray();
        JSONArray error_message_list = new JSONArray();
        JSONArray latency_list = new JSONArray();

        int index = 0;
        for(Document doc: documents){
            id_list.add(index);
            index ++;
            timestamp_list.add(doc.get("timestamp").toString());
            currencyFrom_list.add(doc.get("currencyFrom").toString());
            currencyTo_list.add(doc.get("currencyTo").toString());
            currency_rate_date_list.add(doc.get("currency_rate_date").toString());
            exchange_rate_list.add(String.format("%.2f", doc.get("exchange_rate")));
            error_message_list.add(doc.get("error_message").toString());
            latency_list.add(doc.get("latency").toString());
        }

        obj.put("id", id_list);
        obj.put("timestamp",timestamp_list);
        obj.put("currencyFrom", currencyFrom_list);
        obj.put("currencyTo", currencyTo_list);
        obj.put("currency_rate_date", currency_rate_date_list);
        obj.put("exchange_rate", exchange_rate_list);
        obj.put("error_message", error_message_list);
        obj.put("latency", latency_list);

        String log_data = obj.toJSONString();
        return log_data;

    }

    /**
     * Method getAnalysis retrieve operation analytics using data from the collection
     * @return a map storing analytics
     */
    public Map<String, String> getAnalysis() {

        Map<String, String> map = new HashMap<>();
        ArrayList<Document> documents = this.getDocuments();

        //Retrieve top 3 currencies to be converted from and to
        Map<String, Integer> topCurrenciesFrom = new HashMap<>();
        Map<String, Integer> topCurrenciesTo = new HashMap<>();
        Double avg_latency = 0.0;
        for (Document doc : documents) {
            String cf = doc.get("currencyFrom").toString();
            if (!topCurrenciesFrom.containsKey(cf)) {
                topCurrenciesFrom.put(cf, 0);
            }
            topCurrenciesFrom.replace(cf, topCurrenciesFrom.get(cf) + 1);

            String ct = doc.get("currencyTo").toString();
            if (!topCurrenciesTo.containsKey(ct)) {
                topCurrenciesTo.put(ct, 0);
            }
            topCurrenciesTo.replace(ct, topCurrenciesTo.get(ct) + 1);

            avg_latency += Double.valueOf(doc.get("latency").toString());
        }

        List<Map.Entry<String, Integer>> list1 = new ArrayList<>(topCurrenciesFrom.entrySet());
        list1.sort(Map.Entry.comparingByValue());
        Collections.reverse(list1);

        List<Map.Entry<String, Integer>> list2 = new ArrayList<>(topCurrenciesTo.entrySet());
        list2.sort(Map.Entry.comparingByValue());
        Collections.reverse(list2);

        String s1 = "";
        for (int i = 0; i < Math.min(3, list1.size()); i++) {
            if (i != 0) {
                s1 += ",";
            }
            s1 += Integer.toString(i + 1) + ": " + list1.get(i).getKey();
        }

        String s2 = "";
        for (int i = 0; i < Math.min(3, list2.size()); i++) {
            if (i != 0) {
                s2 += ",";
            }
            s2 += Integer.toString(i + 1) + ": " + list2.get(i).getKey();
        }

        //Retrieve number of accesses to the web service
        map.put("n_accesses", Long.toString(documents.size()));
        map.put("currenciesFrom", s1);
        map.put("currenciesTo", s2);
        //Computes the average latency
        map.put("average_latency", String.format("%.2f ms", avg_latency / documents.size()));

        return map;
    }

}
