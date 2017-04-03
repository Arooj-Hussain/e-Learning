package android.software.com.e_learning;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import android.media.MediaPlayer.OnPreparedListener;

//import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CSEbookList extends AppCompatActivity {

    SharedPreferences sh;
    DownloadManager downloadManager;
    String downloadFileUrl = null;
    private long myDownloadReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cse_book_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setTitle("Title");

        new ConnectServer().execute();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        });

    }

    class ConnectServer extends AsyncTask<String, String, String> {
        SharedPreferences sh;
        String portAddress = getResources().getString(R.string.portAddress);

        @Override
        protected String doInBackground(String... params) {

            //Log.e("ConnectServer","inside async task");

            sh = getSharedPreferences("myChoices", MODE_PRIVATE);
            int gridPosition = sh.getInt("position", -1);
            Intent fromFirst = getIntent();
            Bundle bundle = fromFirst.getExtras();
            String item = bundle.getString("item");
            Log.e("item for subject",""+item);
            item = item.replaceAll(" ", "_");
            Log.e("item for subject",""+item);

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder dataFromServer = new StringBuilder();
            try {
                // Log.e("gridPosition", "first "+gridPosition);
                url = new URL(portAddress+"getBookList.php?gridPosition=" + gridPosition + "&item=" + item);
                // Log.e("ConnectServer","inside try");
                urlConnection = (HttpURLConnection) url.openConnection();
                // Log.e("gridPosition", "second "+gridPosition);

                //BufferedReader br = new BufferedReader(isw);
                BufferedReader br;
                br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String value = "";
                while ((value = br.readLine()) != null) {
                    dataFromServer.append(value);
                }
                Log.e("Value From Server", "inside log " + dataFromServer.toString());
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

            final ArrayList<String> itemFromServer = new ArrayList<>();
            final ArrayList<String> pathFromServer = new ArrayList<>();

            JSONObject json_data = new JSONObject();
            try {
                JSONArray jsonArray = new JSONArray(s);

                //int len = jsonArray.length();

                for (int i = 0; i < jsonArray.length(); i++) {

                    json_data = jsonArray.getJSONObject(i);

                    itemFromServer.add(json_data.getString("item"));
                    pathFromServer.add(json_data.getString("bookPath"));
                }
            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
            }

            Log.e("Names", itemFromServer.toString());
            Log.e("Paths", pathFromServer.toString());
            sh = getSharedPreferences("myChoices", MODE_PRIVATE);
            final int gridPosition = sh.getInt("position", -1);
            Intent fromFirst = getIntent();
            Bundle bundle = fromFirst.getExtras();
            final String item = bundle.getString("item");

            ListView list = (ListView) findViewById(R.id.list1);
            ArrayAdapter adapter = null;
            adapter = new ArrayAdapter(CSEbookList.this, android.R.layout.simple_list_item_1, itemFromServer);
            list.setAdapter(adapter);

            Toast.makeText(CSEbookList.this, "Grid position is " + gridPosition + " and list item is " + item, Toast.LENGTH_SHORT).show();

            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {
                  if(gridPosition==1) { //ie if videos are selected

                      downloadFileUrl = portAddress + pathFromServer.get(position);
                      AlertDialog.Builder builder = new AlertDialog.Builder(CSEbookList.this);
                      // builder.setCancelable(false);
                      builder.setMessage("How do you want to watch?");
                      builder.setPositiveButton("Download First", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {

                              downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                              Log.e("specific path", pathFromServer.get(position));
                              Uri uri = Uri.parse(downloadFileUrl);
                              DownloadManager.Request request = new DownloadManager.Request(uri);
                              request.setDescription("My Download").setTitle("Downloading" + pathFromServer.get(position));
                              request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, pathFromServer.get(position));
                              // request.setDestinationInExternalFilesDir(MainActivity.this,Environment.DIRECTORY_DOWNLOADS,"logo.png");
                              request.setVisibleInDownloadsUi(true);
                              request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                              myDownloadReference = downloadManager.enqueue(request);

                              final Snackbar snack = Snackbar.make(view, "Playing Video After Download Completes, Please Wait", Snackbar.LENGTH_INDEFINITE);
                              snack.show();

                              BroadcastReceiver onComplete=new BroadcastReceiver() {
                                  public void onReceive(Context ctxt, Intent intent) {

                                      snack.dismiss();
                                      File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/Download/"+ pathFromServer.get(position));
                                      Intent target = new Intent(Intent.ACTION_VIEW);
                                      target.setDataAndType(Uri.fromFile(file),"video/mp4");
                                      target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                                      Intent intt = Intent.createChooser(target, "Open File");
                                      try {
                                          Log.e("filePath : ",Environment.getExternalStorageDirectory().getAbsolutePath());
                                          startActivity(intt);
                                      } catch (ActivityNotFoundException e) {
                                          Snackbar.make(view, "No video viewer found on device. Please install one first.", Snackbar.LENGTH_INDEFINITE).show();
                                      }
                                  }
                              };

                              registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                          }
                      });

                      builder.setNegativeButton("Stream Online", new DialogInterface.OnClickListener() {
                          @Override
                          public void onClick(DialogInterface dialog, int which) {
//
//                              // code snippet 1... for streaming in chrome
//
//                            // Build the intent
//                              Uri location = Uri.parse(downloadFileUrl);
//                              Intent intent = new Intent(Intent.ACTION_VIEW, location);
//
//                              // Verify it resolves
//                              PackageManager packageManager = getPackageManager();
//                              List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
//                              boolean isIntentSafe = activities.size() > 0;
//
//                              //  Start an activity if it's safe
//                              if (isIntentSafe) {
//                                  startActivity(intent);
//                              }
                              //code snippet 2... for in-app streaming
                              Intent video = new Intent(CSEbookList.this, VidActivity.class);
                              video.putExtra("videopath",pathFromServer.get(position));
                              Log.e("videopath",pathFromServer.get(position));
                              startActivity(video);
                          }
                      });

                      AlertDialog dialog = builder.create();
                      dialog.show();
                  }
                  else{

                      downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                      Log.e("specific path", pathFromServer.get(position));
                      downloadFileUrl = portAddress + pathFromServer.get(position);
                      Uri uri = Uri.parse(downloadFileUrl);
                      DownloadManager.Request request = new DownloadManager.Request(uri);
                      request.setDescription("My Download").setTitle("Downloading" + pathFromServer.get(position));
                      request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, pathFromServer.get(position));
                      // request.setDestinationInExternalFilesDir(MainActivity.this,Environment.DIRECTORY_DOWNLOADS,"logo.png");
                      request.setVisibleInDownloadsUi(true);
                      request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                      myDownloadReference = downloadManager.enqueue(request);
                      Toast.makeText(CSEbookList.this, "Downloading And Opening File, Please Wait...", Toast.LENGTH_LONG).show();

                      BroadcastReceiver onComplete=new BroadcastReceiver() {
                          public void onReceive(Context ctxt, Intent intent) {

                             File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/Download/"+ pathFromServer.get(position));
                             Intent target = new Intent(Intent.ACTION_VIEW);
                             target.setDataAndType(Uri.fromFile(file),"application/pdf");
                             target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                             Intent intt = Intent.createChooser(target, "Open File");
                             try {
                                 Log.e("filePath : ",Environment.getExternalStorageDirectory().getAbsolutePath());
                                 startActivity(intt);
                             } catch (ActivityNotFoundException e) {
                                 Snackbar.make(view, "No PDF reader found on device. Please install one first.", Snackbar.LENGTH_INDEFINITE).show();
                             }
                          }
                      };

                      registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                  }

                }
            });
        }
    }
}





