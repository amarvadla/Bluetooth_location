package com.example.dhanu.auto_blue;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static java.lang.Double.parseDouble;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSIONS = 100;
    TextView tv,tv2;
    private boolean boolean_permission = false;
    boolean home_loc=false;
    MyService myService;
    BroadcastReceiver broadcastReceiver;
    SharedPreferences.Editor editor;
    SharedPreferences sharedPreferences;
    String lat,lon;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = (TextView)findViewById(R.id.textView4);
        tv2 = (TextView)findViewById(R.id.textView5);
        Button button = (Button)findViewById(R.id.button);
        TextView textView = (TextView) findViewById(R.id.textView3);
        for_permission();
        if(boolean_permission)
            startService(new Intent(getApplicationContext(),MyService.class));

        sharedPreferences = getSharedPreferences("home",MODE_APPEND);
        if(sharedPreferences.getString("bool","").equals("true")){
            button.setVisibility(View.INVISIBLE);
            textView.setVisibility(View.INVISIBLE);
            home_loc=true;
        }
    }

    public void save(View view){
        sharedPreferences = getSharedPreferences("home",MODE_APPEND);
        editor = sharedPreferences.edit();
        editor.putString("lat",lat);
        editor.putString("lon",lon);
        editor.putString("bool","true");
        editor.commit();
        Button button = (Button)findViewById(R.id.button);
        TextView textView = (TextView) findViewById(R.id.textView3);
        button.setVisibility(View.INVISIBLE);
        textView.setVisibility(View.INVISIBLE);
        home_loc=true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(MainActivity.this);
        menuInflater.inflate(R.menu.mymenu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.set:
                Button button = (Button)findViewById(R.id.button);
                TextView textView = (TextView) findViewById(R.id.textView3);
                button.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                lat = String.valueOf(intent.getExtras().get("Latitude"));
                lon = String.valueOf(intent.getExtras().get("Longitude"));
                List<Address> addresses;
                Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                try {
                    addresses = geocoder.getFromLocation(parseDouble(lat),parseDouble(lon), 1);
                    String address = addresses.get(0).getAddressLine(0);
                    tv.setText(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                float[] dist = new float[1];
                sharedPreferences = getSharedPreferences("home",MODE_APPEND);
                if(home_loc) {
                    Location.distanceBetween(parseDouble(sharedPreferences.getString("lat","0")), parseDouble(sharedPreferences.getString("lon","0")), parseDouble(lat), parseDouble(lon), dist);
                    if (dist[0] / 200 < 1) {
                        tv2.setText("you are at home");
                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (!mBluetoothAdapter.isEnabled()) {
                            mBluetoothAdapter.enable();
                        }
                    }
                    else{
                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        mBluetoothAdapter.disable();
                        tv2.setText("seems your not at home");
                    }

                }
            }
        };

        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    private boolean runtime_permission() {
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission
                .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest
                .permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean_permission = true;
                    startService(new Intent(getApplicationContext(),MyService.class));

                } else {
                    Toast.makeText(getApplicationContext(), "Please allow the permission", Toast.LENGTH_LONG).show();
                    runtime_permission();
                }
            }
        }
    }

    private void for_permission() {
        if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {

            if ((ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION))) {


            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION

                        },
                        REQUEST_PERMISSIONS);

            }
        } else {
            boolean_permission = true;
        }
    }
}
