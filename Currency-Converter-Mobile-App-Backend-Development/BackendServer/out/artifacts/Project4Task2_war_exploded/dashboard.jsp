<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.json.simple.JSONObject" %>
<%@ page import="org.json.simple.parser.ParseException" %>
<%@ page import="org.json.simple.JSONArray" %><%--
  Created by IntelliJ IDEA.
  User: Bertha
  Date: 2019/9/21
  Time: 上午 12:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<html>
    <head>
        <title style="font-size:200%;" > Currency Converter Service Dashboard. </title>
    </head>
    <style>
        table {
            font-family: arial, sans-serif;
            border-collapse: collapse;
            width: 80%;
        }

        td, th {
            border: 1px solid #dddddd;
            text-align: left;
            padding: 8px;
        }
    </style>
    <body>
    <p style="font-size:150%;">This section displays some interesting operation Analytics.</p>
    Number of mobile accesses: <%= request.getAttribute("n_accesses")%><br>
    Top 3 popular currencies to be converted from: <%= request.getAttribute("currenciesFrom")%><br>
    Top 3 popular currencies to be converted to: <%= request.getAttribute("currenciesTo")%><br>
    Average search latency: <%= request.getAttribute("average_latency")%><br><br>

    <p style="font-size:150%;">This section displays past log data.</p>
    <table>
        <% String logs = (String) request.getAttribute("logs"); %>
        <% JSONParser parser = new JSONParser(); %>
        <% JSONObject obj = new JSONObject(); %>
        <% try {%>
        <%      obj = (JSONObject) parser.parse(logs); %>
        <% } catch (ParseException e) {%>
        <%      e.printStackTrace();%>
        <% }%>
        <tr>
            <th>log_id</th>
            <th>timestamp</th>
            <th>currencyFrom</th>
            <th>currencyTo</th>
            <th>currency_rate_date</th>
            <th>exchange_rate</th>
            <th>error_message</th>
            <th>latency</th>
        </tr>
        <% JSONArray id_list = (JSONArray) obj.get("id");%>
        <% JSONArray timestamp_list = (JSONArray) obj.get("timestamp");%>
        <% JSONArray currencyFrom_list = (JSONArray) obj.get("currencyFrom");%>
        <% JSONArray currencyTo_list = (JSONArray) obj.get("currencyTo");%>
        <% JSONArray currency_rate_date_list = (JSONArray) obj.get("currency_rate_date");%>
        <% JSONArray exchange_rate_list= (JSONArray) obj.get("exchange_rate");%>
        <% JSONArray error_message_list = (JSONArray) obj.get("error_message");%>
        <% JSONArray latency_list = (JSONArray) obj.get("latency");%>
        <% for(int i = 0; i < id_list.size(); i++) { %>
        <tr>
            <td> <%= id_list.get(i) %> </td>
            <td> <%= timestamp_list.get(i) %> </td>
            <td> <%= currencyFrom_list.get(i) %> </td>
            <td> <%= currencyTo_list.get(i) %> </td>
            <td> <%= currency_rate_date_list.get(i) %> </td>
            <td> <%= exchange_rate_list.get(i) %> </td>
            <td> <%= error_message_list.get(i) %> </td>
            <td> <%= latency_list.get(i) %> </td>
        </tr>
        <% } %>
    </table>
    </body>
</html>
