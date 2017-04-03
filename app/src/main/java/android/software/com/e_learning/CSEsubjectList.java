package android.software.com.e_learning;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class CSEsubjectList extends AppCompatActivity {

    SharedPreferences sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cse_subject_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sh = getSharedPreferences("myChoices",MODE_PRIVATE);
        int gridPosition = sh.getInt("position",-1);
        if (gridPosition == 3)
            setTitle("Examination Year");
        else
            setTitle("Subjects");

        new ConnectServer().execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    class ConnectServer extends AsyncTask<String, String, String>
    {
        SharedPreferences sh;
        String portAddress =
                getResources().getString(R.string.portAddress);

        @Override
        protected String doInBackground(String... params) {

            Log.e("ConnectServer","inside async task");
            sh = getSharedPreferences("myChoices",MODE_PRIVATE);
            int gridPosition = sh.getInt("position",-1);
            String deptt = sh.getString("deptt","");

            Log.e("position",""+gridPosition);
            Log.e("deptt in subject list",""+deptt);

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder dataFromServer = new StringBuilder();
            try {
                Log.e("gridPosition", "first "+gridPosition);
                url = new URL(portAddress+"/getSubjectList.php?Department="+deptt+"&gridPosition=" + gridPosition);
                Log.e("ConnectServer","inside try");
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.e("gridPosition", "second "+gridPosition);

                //BufferedReader br = new BufferedReader(isw);
                BufferedReader br;
                br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String value = "";
                while((value = br.readLine()) != null)
                {
                    dataFromServer.append(value);
                }
                Log.e("Value From Server", "inside log "+dataFromServer.toString());
            } catch (MalformedURLException e) {
                Log.e("Exception : ", e.toString());
            } catch (IOException e) {
                Log.e("Exception : ", e.toString());
            }

            String combined = dataFromServer.toString();
            return combined;

        }

        @Override
        protected void onPostExecute(String s) {
//            String[] separated = s.split("#");
//            Toast.makeText(CSEsubjectList.this, s, Toast.LENGTH_SHORT).show();

            ArrayList<String> itemFromServer = new ArrayList<>();
             JSONObject json_data = new JSONObject();
            try {
                JSONArray jsonArray = new JSONArray(s);

                //int len = jsonArray.length();

                for (int i = 0; i < jsonArray.length(); i++) {

                    json_data = jsonArray.getJSONObject(i);

                    itemFromServer.add(json_data.getString("item"));
                  }
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }

            Log.e("Names", itemFromServer.toString());


//            sh = getSharedPreferences("myChoices", MODE_PRIVATE);
//            int gridPosition = sh.getInt("position", -1);

            ListView list = (ListView) findViewById(R.id.list);
            ArrayAdapter adapter = null;
            adapter = new ArrayAdapter(CSEsubjectList.this, android.R.layout.simple_list_item_1, itemFromServer);
            list.setAdapter(adapter);


            final ArrayAdapter finalAdapter = adapter;//needs to be declared final to be accessed inside the inner class below.
            // so we put it into another variable "finalAdapter" bcz "adapter" needs to change

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    //toast and log..just for checking

                    String iValue = ((Integer) position).toString();
                    //  Toast.makeText(CSEsubjectList.this, "Item Clicked at position " + iValue , Toast.LENGTH_LONG).show();
                    Log.e("Status", iValue);
                    //main on click job
                    Intent intent = new Intent(CSEsubjectList.this, CSEbookList.class);
                    intent.putExtra("item", finalAdapter.getItem(position).toString());
                    startActivity(intent);

                }
            });
        }

        }
    }


