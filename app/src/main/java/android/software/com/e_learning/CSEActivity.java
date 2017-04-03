package android.software.com.e_learning;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.CardView;
import android.util.Log;
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
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class CSEActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    SharedPreferences sh;
    SharedPreferences.Editor edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cse);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.e("testing ","here i am");

        sh = this.getSharedPreferences("myChoices",MODE_PRIVATE);
        edit = sh.edit();

        String deptt = sh.getString("deptt","");
        Log.e("Department ",deptt);
        if(deptt == "CSE")
            setTitle("Computer Sc. Engineering");
        else if(deptt == "ENC")
            setTitle("Electronics & Comm. Engg");
        else
            setTitle(deptt + " Engineering");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle( this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    public void card_onClick(View v) {
        Intent intent = new Intent(CSEActivity.this, CSEsubjectList.class);
        if(v.getId() == R.id.textView_ebooks)
        {
            edit.putInt("position",0);
        }
        else if(v.getId() == R.id.cardView_videos)
        {
            edit.putInt("position",1);
        }
        else if(v.getId() == R.id.cardView_notes)
        {
            edit.putInt("position",2);
        }
        else if(v.getId() == R.id.cardView_gate)
        {
            edit.putInt("position",3);
        }
            edit.commit();
            startActivity(intent);
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


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_cse) {
            edit.putString("deptt","CSE");
            setTitle("Computer Sc. Engineering");
        } else if (id == R.id.nav_enc) {
            edit.putString("deptt","ENC");
            setTitle("Electronics & Comm. Engg");
        }
        else if (id == R.id.nav_civil) {
            edit.putString("deptt","Civil");
            setTitle("Civil Engineering");
        }
        else if (id == R.id.nav_mechanical) {
            edit.putString("deptt","Mechanical");
            this.setTitle("Mechanical Engineering");

        }
        else if (id == R.id.nav_electrical) {
            edit.putString("deptt","Electrical");
            setTitle("Electrical Engineering");
        }

        edit.apply();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
