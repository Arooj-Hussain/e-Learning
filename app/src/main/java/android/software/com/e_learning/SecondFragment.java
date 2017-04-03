package android.software.com.e_learning;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

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


public class SecondFragment extends Fragment {
    // Store instance variables
    private String title;
    private int page;
    SharedPreferences sh;
    DownloadManager downloadManager;
    String downloadFileUrl = null;
    private long myDownloadReference;
    View view;

    // newInstance constructor for creating fragment with arguments
    public static SecondFragment newInstance(int page, String title) {
        SecondFragment fragmentFirst = new SecondFragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }

    // Store instance variables based on arguments passed
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    // Inflate the view for the fragment based on layout XML
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_second, container, false);
//        TextView tvLabel = (TextView) view.findViewById(R.id.tvLabelSecond);
//        tvLabel.setText(page + " -- " + title);
        Log.e("inside " , "onCreateView");
        new ConnectServer().execute();
        Log.e("inside2 " , "onCreateView");
        return view;


    }




    class ConnectServer extends AsyncTask<String, String, String> {
        SharedPreferences sh;
        String portAddress = getResources().getString(R.string.portAddress);

        @Override
        protected String doInBackground(String... params) {

            Log.e("ConnectServer","inside async task");

//        sh = getSharedPreferences("myChoices", MODE_PRIVATE);
//        int gridPosition = sh.getInt("position", -1);
//        Intent fromFirst = getIntent();
//        Bundle bundle = fromFirst.getExtras();
//        String item = bundle.getString("item");


            //Log.e("position",""+gridPosition);

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder dataFromServer = new StringBuilder();
            try {
                // Log.e("gridPosition", "first "+gridPosition);
                url = new URL(portAddress+"topdownloads.php");
                Log.e("ConnectServer","inside try");
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
            Log.e("inside " , "onPostExec");
            final ArrayList<String> itemFromServer = new ArrayList<>();
            final ArrayList<String> pathFromServer = new ArrayList<>();

            JSONObject json_data;
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
            final ArrayList type = pathFromServer;
            final String check = "videos";

            //  sh = getSharedPreferences("myChoices", MODE_PRIVATE);
//      final int gridPosition = sh.getInt("position", -1);
//        Intent fromFirst = getIntent();
//        Bundle bundle = fromFirst.getExtras();
//        final String item = bundle.getString("item");

            ListView list = (ListView) view.findViewById(R.id.listViewid);
            ArrayAdapter adapter = null;
            adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, itemFromServer);
            list.setAdapter(adapter);


//        //Toast.makeText(CSEbookList.this, "Grid position is " + gridPosition + " and list item is " + item, Toast.LENGTH_SHORT).show();
//
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {


                    if (pathFromServer.get(position).contains(check)) { //ie if videos are selected

                        downloadFileUrl = portAddress + pathFromServer.get(position);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        // builder.setCancelable(false);
                        builder.setMessage("How do you want to watch?");
                        builder.setPositiveButton("Download First", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
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

                                BroadcastReceiver onComplete = new BroadcastReceiver() {
                                    public void onReceive(Context ctxt, Intent intent) {

                                        snack.dismiss();
                                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + pathFromServer.get(position));
                                        Intent target = new Intent(Intent.ACTION_VIEW);
                                        target.setDataAndType(Uri.fromFile(file), "video/mp4");
                                        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                                        Intent intt = Intent.createChooser(target, "Open File");
                                        try {
                                            Log.e("filePath : ", Environment.getExternalStorageDirectory().getAbsolutePath());
                                            startActivity(intt);
                                        } catch (ActivityNotFoundException e) {
                                            Snackbar.make(view, "No video viewer found on device. Please install one first.", Snackbar.LENGTH_INDEFINITE).show();
                                        }
                                    }
                                };

                                getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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
                                Intent video = new Intent(getContext(), VidActivity.class);
                                video.putExtra("videopath", pathFromServer.get(position));
                                Log.e("videopath", pathFromServer.get(position));
                                startActivity(video);
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    } else {

                        downloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
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
//                    URL url;
//                    HttpURLConnection urlConnection = null;
//                    StringBuilder dataFromServer = new StringBuilder();
//                    try {
//                        // Log.e("gridPosition", "first "+gridPosition);
//                        url = new URL(portAddress + "count.php?path=" + pathFromServer);
//                        // Log.e("ConnectServer","inside try");
//                        urlConnection = (HttpURLConnection) url.openConnection();
//                        // Log.e("gridPosition", "second "+gridPosition);
//
//
//                    } catch (MalformedURLException e) {
//                        Log.e("Exception : ", e.toString());
//                    } catch (IOException e) {
//                        Log.e("Exception : ", e.toString());
//                    }


                        Toast.makeText(getContext(), "Downloading And Opening File, Please Wait...", Toast.LENGTH_LONG).show();

                        BroadcastReceiver onComplete = new BroadcastReceiver() {
                            public void onReceive(Context ctxt, Intent intent) {

                                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + pathFromServer.get(position));
                                Intent target = new Intent(Intent.ACTION_VIEW);
                                target.setDataAndType(Uri.fromFile(file), "application/pdf");
                                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                                Intent intt = Intent.createChooser(target, "Open File");
                                try {
                                    Log.e("filePath : ", Environment.getExternalStorageDirectory().getAbsolutePath());
                                    startActivity(intt);
                                } catch (ActivityNotFoundException e) {
                                    Snackbar.make(view, "No PDF reader found on device. Please install one first.", Snackbar.LENGTH_INDEFINITE).show();
                                }
                            }
                        };

                        getActivity().registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                    }
                }
            });

        }

    }





}
