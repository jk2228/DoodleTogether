package com.jisha.DoodleTogether;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


public class Draw extends Activity {
	private int vid=1;
	static final int REQUEST_CODE = 1001;
	int[] color;

	MyDrawView drawView;

	public int findId() {
		View v = findViewById(vid);
		while (v != null) {
			v = findViewById(++vid);
		}
		return vid;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		color = new int[3];

		setContentView(R.layout.doodle_alone);
		vid = findId();

		drawView = new MyDrawView(this);
		drawView.setId(vid);
		ViewGroup v = (ViewGroup) findViewById(R.id.linear_draw_layout);
		v.addView(drawView);
		//drawView.requestFocus();
	}

	@Override
	public void onStart() 
	{Log.d("Draw","onStartCalled");
		super.onStart();

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	

	}

	public void launchColor(View v) {
		LaunchColorPicker();
		// drawView = new DrawView(this);
		// ViewGroup v2= (ViewGroup)findViewById(R.id.linear_draw_layout);
		// v2.addView(drawView);
		// drawView.requestFocus();
	}

	public void clearScreen(View v) {
		MyDrawView draw = (MyDrawView) findViewById(vid);
		draw.clear();
	}

	private void LaunchColorPicker() {
		Intent launchcolorpicker = new Intent();
		launchcolorpicker.setClassName(
				"somitsolutions.training.android.colorpicker",
				"somitsolutions.training.android.colorpicker.ColorPicker");
		launchcolorpicker
				.setAction("somitsolutions.training.android.colorpicker.android.intent.action.COLORPICKER");
		launchcolorpicker.addCategory("CATEGORY_DEFAULT");
		launchcolorpicker
				.setType("vnd.somitsolutions.color/vnd.somitsolutions.color-value");

		try {
			startActivityForResult(launchcolorpicker, REQUEST_CODE);

		} catch (ActivityNotFoundException e) {
			Log.e("IntentExample", "Activity could not be started...");
		}
	}

	public void onActivityResult(int requestcode, int resultcode, Intent result) {
		if (requestcode == REQUEST_CODE) {
			if (resultcode == RESULT_OK) {
				color[0] = (int) (result
						.getDoubleArrayExtra("somitsolutions.training.android.colorpicker.color_of_the_shape"))[0];
				color[1] = (int) (result
						.getDoubleArrayExtra("somitsolutions.training.android.colorpicker.color_of_the_shape"))[1];
				color[2] = (int) (result
						.getDoubleArrayExtra("somitsolutions.training.android.colorpicker.color_of_the_shape"))[2];
				int value = Color.rgb(color[0], color[1], color[2]);
				((MyDrawView) findViewById(vid)).changeColor(value);//not working yet

			}
		}
	}
}