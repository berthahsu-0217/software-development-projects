package edu.cmu.minhsuah;

/*
 * @author Bertha Hsu
 * This script is used to construct the main acitivity of the app.
 * It contains code to build user interface and do some simple calculation using the exchange rate received.
 */

import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    String currencyFrom;
    String currencyTo;

    //Construct the main activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final MainActivity ma = this;

        //Display currencies on spinners
        final Spinner spinnerFrom = (Spinner) findViewById(R.id.spinner);
        final Spinner spinnerTo = (Spinner) findViewById(R.id.spinner2);
        final String[] currencies = {"AUD","BGN","BRL","CAD","CHF","CNY","CZK","DKK","EUR","GBP","HKD","HRK","HUF","IDR","ILS","INR","ISK","JPY","KRW","MXN","MYR","NOK","NZD","PHP","PLN","RON","RUB","SEK","SGD","THB","TRY","USD","ZAR"};
        ArrayAdapter<String> currencyList = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, currencies);
        spinnerFrom.setAdapter(currencyList);
        spinnerTo.setAdapter(currencyList);

        //Add a listener to the convert button
        Button submitButton = (Button) findViewById(R.id.button);
        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View viewParam) {
                String currency0 = String.valueOf(spinnerFrom.getSelectedItem());
                String currency1 = String.valueOf(spinnerTo.getSelectedItem());
                currencyFrom = currency0;
                currencyTo = currency1;
                RetrieveCurrencyRate task = new RetrieveCurrencyRate();
                task.search(currency0, currency1, ma);
            }
        });

    }

    /**
     * Method convert takes the exchange rate, and converts the entered amount to the corresponding currency.
     * @param rate exchange rate
     */
    public void convert(Double rate){

        Double amount = Double.valueOf(((EditText) findViewById(R.id.editAmount)).getText().toString());
        TextView messageView = findViewById(R.id.message);

        if (rate < 0) {
            messageView.setText("Failed to find exchange rate.");
        } else {
            messageView.setText(String.format("%.2f in %s equals to %.2f in %s", amount, currencyFrom, amount*rate, currencyTo));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle action bar item clicks here. The action bar will
        //automatically handle clicks on the Home/Up button, so long
        //as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
