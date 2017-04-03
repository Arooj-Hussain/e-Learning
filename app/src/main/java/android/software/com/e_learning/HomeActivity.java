package android.software.com.e_learning;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Fragment;

import com.google.firebase.messaging.FirebaseMessaging;

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

public class HomeActivity extends ActionBarActivity implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences sh;
    SharedPreferences.Editor edit;
    private static final String TAG = HomeActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private TextView txtRegId, txtMessage;
    DownloadManager downloadManager;
    String downloadFileUrl = null;
    private long myDownloadReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sh = this.getSharedPreferences("myChoices", MODE_PRIVATE);
        edit = sh.edit();
        txtRegId = (TextView) findViewById(R.id.txt_reg_id);
        txtMessage = (TextView) findViewById(R.id.txt_push_message);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {




                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);

                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");
                    Log.e(TAG + "Recieved : ", message);
                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();

                    txtMessage.setText(message);
                }
            }
        };

        displayFirebaseRegId();

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        final ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);

        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.addTab(tabLayout.newTab().setText("Home"));
        tabLayout.addTab(tabLayout.newTab().setText("Top Downloads"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final PageAdapter adapter = new PageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {


            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });



        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        Toast t;
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(info.getType() == ConnectivityManager.TYPE_MOBILE || info.getType() == ConnectivityManager.TYPE_WIFI) {
            boolean flag = info.isConnectedOrConnecting();
            if (flag) {
                t = Toast.makeText(HomeActivity.this, "Internet Connected", Toast.LENGTH_LONG);
                t.setGravity(Gravity.CENTER, 0, 0);
                t.show();
            } else {
                t = Toast.makeText(HomeActivity.this, "Not Connected to any Network...", Toast.LENGTH_LONG);
                t.setGravity(Gravity.BOTTOM, 0, 50);
                t.show();
            }
        }

        Button closeButton = (Button) findViewById(R.id.buttonClose);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setCancelable(false);
                builder.setMessage("Do you really want to exit?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        HomeActivity.this.finish();
                    }
                });

                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.e("inside","oncreateOptionsMenu of Search activity");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ImageView image = (ImageView) findViewById(R.id.imageView);
                image.setVisibility(View.GONE);


//                ListView list = (ListView) findViewById(R.id.listViewid);
//                list.setVisibility(View.GONE);
//

                Log.e("query is ",query);
                new ConnectServer().execute(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }



    class ConnectServer extends AsyncTask<String, String, String> {
        SharedPreferences sh;
        String portAddress = getResources().getString(R.string.portAddress);

        // String value = editText.getText().toString();
        @Override
        protected String doInBackground(String... params) {
            // Log.e("Value_of_query2",query);
            Log.e("ConnectServer", "inside async task");
            //  sh = getSharedPreferences("myChoices", MODE_PRIVATE);
            //  int gridPosition = sh.getInt("position", -1);
            // String deptt = sh.getString("deptt", "");

            //  Log.e("position", "" + gridPosition);
            //   Log.e("deptt in subject list", "" + deptt);

            URL url;
            HttpURLConnection urlConnection = null;
            StringBuilder dataFromServer = new StringBuilder();
            try {
                //  Log.e("gridPosition", "first " + gridPosition);
                // url = new URL(portAddress+"/getSubjectList.php?Department="+deptt+"&gridPosition=" + gridPosition);
                // Log.e("Value_of_query3",query);
                url = new URL(portAddress+"dataSearch.php?searchQuery="+params[0]);

                Log.e("ConnectServer", "inside try");
                urlConnection = (HttpURLConnection) url.openConnection();
                // Log.e("gridPosition", "second " + gridPosition);

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
//            String[] separated = s.split("#");
//            Toast.makeText(CSEsubjectList.this, s, Toast.LENGTH_SHORT).show();
            Log.e("Inside","onPostExecute");
            ArrayList<String> itemFromServer = new ArrayList<>();
            final ArrayList<String> pathFromServer = new ArrayList<>();

            JSONObject json_data = new JSONObject();
            try {
                JSONArray jsonArray = new JSONArray(s);

                int len = jsonArray.length();

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
            final String type = pathFromServer.toString();
            final String check = "videos";


            //  sh = getSharedPreferences("myChoices", MODE_PRIVATE);
            // int gridPosition = sh.getInt("position", -1);

            ListView list = (ListView) findViewById(R.id.list);
            ArrayAdapter adapter = null;
            adapter = new ArrayAdapter(HomeActivity.this, android.R.layout.simple_list_item_1, itemFromServer);
            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

//
                    if(type.contains(check)) { //ie if videos are selected

                        downloadFileUrl = portAddress + pathFromServer.get(position);
                        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                        // builder.setCancelable(false);
                        builder.setMessage("How do you want to watch?");
                        builder.setPositiveButton("Download First", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                                Log.e("specific path", pathFromServer.get(position));

                                Log.e("downloadFileUrl",downloadFileUrl);

                                Uri uri = Uri.parse(downloadFileUrl);
                                DownloadManager.Request request = new DownloadManager.Request(uri);
                                request.setDescription("My Download").setTitle("Downloading" + pathFromServer.get(position));
                                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, pathFromServer.get(position));
                                // request.setDestinationInExternalFilesDir(MainActivity.this,Environment.DIRECTORY_DOWNLOADS,"logo.png");
                                request.setVisibleInDownloadsUi(true);
                                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                                myDownloadReference = downloadManager.enqueue(request);

                                // final Snackbar snack = Snackbar.make(view, "Playing Video After Download Completes, Please Wait", Snackbar.LENGTH_INDEFINITE);
                                // snack.show();

                                BroadcastReceiver onComplete=new BroadcastReceiver() {
                                    public void onReceive(Context ctxt, Intent intent) {

                                        //   snack.dismiss();
                                        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/Download/"+ pathFromServer.get(position));
                                        Intent target = new Intent(Intent.ACTION_VIEW);
                                        target.setDataAndType(Uri.fromFile(file),"video/mp4");
                                        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                                        Intent intt = Intent.createChooser(target, "Open File");
                                        try {
                                            Log.e("filePath : ",Environment.getExternalStorageDirectory().getAbsolutePath());
                                            startActivity(intt);
                                        } catch (ActivityNotFoundException e) {
                                            //  Snackbar.make(view, "No video viewer found on device. Please install one first.", Snackbar.LENGTH_INDEFINITE).show();
                                        }
                                    }
                                };

                                registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                            }
                        });

                        builder.setNegativeButton("Stream Online", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

//                              // code snippet 1... for streaming in chrome or any other browser
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
//                             if (isIntentSafe) {
//                                 startActivity(intent);
//                              }
//
                                //code snippet 2... for in-app streaming
                                Intent video = new Intent(HomeActivity.this, VidActivity.class);
                                video.putExtra("videopath",pathFromServer.get(position));
                                Log.e("videopath",pathFromServer.get(position));
                                startActivity(video);
                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                    else
                    {

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
                        Toast.makeText(HomeActivity.this, "Downloading And Opening File, Please Wait...", Toast.LENGTH_LONG).show();

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
//                                    Snackbar.make(view, "No PDF reader found on device. Please install one first.", Snackbar.LENGTH_INDEFINITE).show();
                                }
                            }
                        };

                        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

                    }
                }
            });


        }


    }

    public class PageAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        public PageAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
            Log.e("PageAdapter","inside constructor");
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FirstFragment();
                case 1:
                    return new SecondFragment();

                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e(TAG, "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId))
            txtRegId.setText("Firebase Reg Id: " + regId);
        else
            txtRegId.setText("Firebase Reg Id is not received yet!");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "Resumed...");
        // register FCM registration complete receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));

        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));

        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//may be you need to remove this
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
//till here
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Fragment fragment;

        if (id == R.id.nav_cse) {
            // startActivity(new Intent(HomeActivity.this,CSEActivity.class));
            edit.putString("deptt","CSE");
             Intent intent = new Intent(HomeActivity.this, CSEActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);


        } else if (id == R.id.nav_enc) {
            edit.putString("deptt","ENC");
            Intent intent = new Intent(HomeActivity.this, CSEActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (id == R.id.nav_civil) {
            edit.putString("deptt","Civil");
            Intent intent = new Intent(HomeActivity.this, CSEActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (id == R.id.nav_mechanical) {
            edit.putString("deptt","Mechanical");
            Intent intent = new Intent(HomeActivity.this, CSEActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        } else if (id == R.id.nav_electrical) {
            edit.putString("deptt","Electrical");
            Intent intent = new Intent(HomeActivity.this, CSEActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);

        }

        edit.apply();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
