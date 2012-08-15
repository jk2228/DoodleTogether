package com.jbh.DoodleTogether;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class Home extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.home);
    }
    
    public void launchDraw(View v) {
        Intent intent = new Intent(this, Draw.class);
        startActivity(intent);
    }
    
    public void startSession(View v) {
        Intent intent = new Intent(this, BluetoothConnect.class);
        startActivity(intent);
    }
}
