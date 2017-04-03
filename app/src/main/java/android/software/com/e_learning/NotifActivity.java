package android.software.com.e_learning;


import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class NotifActivity extends ActionBarActivity {

    TextView txtMessage;
    TextView txt_bookTitle;
    TextView txt_subject;
    TextView txt_author;
    TextView txt_publisher;
    DownloadManager downloadManager;
    String downloadFileUrl;
    long myDownloadReference;
    String path;
    JSONObject payload;
    String portAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);

        portAddress = getResources().getString(R.string.portAddress);
        txtMessage = (TextView) findViewById(R.id.textView_notif);
        txt_bookTitle = (TextView) findViewById(R.id.textView_title);
        txt_author = (TextView) findViewById(R.id.textView_author);
        txt_subject = (TextView) findViewById(R.id.textView_subject);
        txt_publisher = (TextView) findViewById(R.id.textView_publisher);

        //Open this on clicking a notification...

        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                if (key.equals("data")) {
                    String value = getIntent().getExtras().getString(key);

                    try {
                        JSONObject json = new JSONObject(value);
                        //  Log.e("json", json.toString());
                        //JSONObject data = json.getJSONObject("data");
                        //  Log.e("json",data.toString());
                        //Log.d("data", json.toString());
                        String title = json.getString("title");
                        String message = json.getString("message");
                        payload = json.getJSONObject("payload");

                        if (payload.toString().contains("ebook")) {

                            String insidePayload = payload.getString("text");
                            String bookTitle = payload.getString("title");
                            String subject = payload.getString("subject");
                            String author = payload.getString("author");
                            String publisher = payload.getString("publisher");
                            path = payload.getString("path");

                            txtMessage.setText(insidePayload);
                            txt_subject.setText("Subject : ");
                            txt_subject.append(subject);
                            txt_bookTitle.setText("Title : ");
                            txt_bookTitle.append(bookTitle);
                            txt_author.setText("Author : ");
                            txt_author.append(author);
                            txt_publisher.setText("Publisher : ");
                            txt_publisher.append(publisher);

                        }else if (payload.toString().contains("notes")) {

                            String insidePayload = payload.getString("text");
                            String notesTitle = payload.getString("title");
                            String subject = payload.getString("subject");
                            String topic = payload.getString("topic");
                            path = payload.getString("path");

                            txtMessage.setText(insidePayload);
                            txt_subject.setText("Subject : ");
                            txt_subject.append(subject);
                            txt_bookTitle.setText("Title : ");
                            txt_bookTitle.append(notesTitle);
                            txt_author.setText("Topic : ");
                            txt_author.append(topic);


                        }else if (payload.toString().contains("video")) {

                            String insidePayload = payload.getString("text");
                            String videoTitle = payload.getString("title");
                            String subject = payload.getString("subject");
                            String instructor = payload.getString("instructor");
                            String topic = payload.getString("topic");
                            path = payload.getString("path");

                            txtMessage.setText(insidePayload);
                            txt_subject.setText("Subject : ");
                            txt_subject.append(subject);
                            txt_bookTitle.setText("Title : ");
                            txt_bookTitle.append(videoTitle);
                            txt_author.setText("Instructor : ");
                            txt_author.append(instructor);
                            txt_publisher.setText("Topic : ");
                            txt_publisher.append(topic);

                        }else if (payload.toString().contains("GATE Paper")) {

                            String insidePayload = payload.getString("text");
                            String paperTitle = payload.getString("title");
                            String year = payload.getString("year");
                            String department = payload.getString("department");
                            path = payload.getString("path");

                            txtMessage.setText(insidePayload);
                            txt_subject.setText("Year : ");
                            txt_subject.append(year);
                            txt_bookTitle.setText("Title : ");
                            txt_bookTitle.append(paperTitle);
                            txt_author.setText("Department : ");
                            txt_author.append(department);
                        }

//                        Log.e("title", title);
//                        Log.e("message", message);
//                        Log.e("payload", payload.toString());
//                        Log.e("InsidePayload", insidePayload);
//                        Log.e("bookTitle", bookTitle);
//                        Log.e("path", path);
//                        Log.e("author", author);
//                        Log.e("publisher", publisher);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }
        }


    }

    public void title_onClick(View v) {

        if (payload.toString().contains("video")) {
            //ie if videos are selected

            downloadFileUrl = portAddress + path;
            AlertDialog.Builder builder = new AlertDialog.Builder(NotifActivity.this);
            // builder.setCancelable(false);
            builder.setMessage("How do you want to watch?");
            builder.setPositiveButton("Download First", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                    Log.e("specific path", path);
                    Uri uri = Uri.parse(downloadFileUrl);
                    DownloadManager.Request request = new DownloadManager.Request(uri);
                    request.setDescription("My Download").setTitle("Downloading" + path);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, path);
                    // request.setDestinationInExternalFilesDir(MainActivity.this,Environment.DIRECTORY_DOWNLOADS,"logo.png");
                    request.setVisibleInDownloadsUi(true);
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    myDownloadReference = downloadManager.enqueue(request);

                    final Snackbar snack = Snackbar.make(findViewById(android.R.id.content), "Playing Video After Download Completes, Please Wait", Snackbar.LENGTH_INDEFINITE);
                    snack.show();

                    BroadcastReceiver onComplete = new BroadcastReceiver() {
                        public void onReceive(Context ctxt, Intent intent) {

                            snack.dismiss();
                            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + path);
                            Intent target = new Intent(Intent.ACTION_VIEW);
                            target.setDataAndType(Uri.fromFile(file), "video/mp4");
                            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                            Intent intt = Intent.createChooser(target, "Open File");
                            try {
                                Log.e("filePath : ", Environment.getExternalStorageDirectory().getAbsolutePath());
                                startActivity(intt);
                            } catch (ActivityNotFoundException e) {
                                Snackbar.make(findViewById(android.R.id.content), "No video viewer found on device. Please install one first.", Snackbar.LENGTH_INDEFINITE).show();
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
                    Intent video = new Intent(NotifActivity.this, VidActivity.class);
                    video.putExtra("videopath", path);
                    Log.e("videopath", path);
                    startActivity(video);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {

            downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Log.e("specific path", path);
            downloadFileUrl = portAddress + path;
            Uri uri = Uri.parse(downloadFileUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setDescription("My Download").setTitle("Downloading" + path);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, path);
            // request.setDestinationInExternalFilesDir(MainActivity.this,Environment.DIRECTORY_DOWNLOADS,"logo.png");
            request.setVisibleInDownloadsUi(true);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            myDownloadReference = downloadManager.enqueue(request);
            Toast.makeText(NotifActivity.this, "Downloading And Opening File, Please Wait...", Toast.LENGTH_LONG).show();

            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {

                    File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/" + path);
                    Intent target = new Intent(Intent.ACTION_VIEW);
                    target.setDataAndType(Uri.fromFile(file), "application/pdf");
                    target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                    Intent intt = Intent.createChooser(target, "Open File");
                    try {
                        Log.e("filePath : ", Environment.getExternalStorageDirectory().getAbsolutePath());
                        startActivity(intt);
                    } catch (ActivityNotFoundException e) {
                        Snackbar.make(findViewById(android.R.id.content), "No PDF reader found on device. Please install one first.", Snackbar.LENGTH_INDEFINITE).show();
                    }
                }
            };

            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        }

    }
}

