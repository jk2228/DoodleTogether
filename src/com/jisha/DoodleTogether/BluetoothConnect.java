/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Downloaded 7/25/2012 modified

package com.jisha.DoodleTogether;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * This is the main Activity that displays the current chat session.
 */

@SuppressLint("ParserError")
public class BluetoothConnect extends Activity {
	// Debugging
	private static final String TAG = "BluetoothConnect";
	private static final boolean D = true;

	// Message types sent from the BluetoothConnectService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_CLEAR = 0;

	// Key names received from the BluetoothConnectService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private boolean toast= true;

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	private static final int REQUEST_CODE = 4;
	private static final int PICKED_COLOR = 5;
	

	static int id = 1;

	MyDrawView drawView;

	// Layout Views
	// private ListView mConversationView;
	// private EditText mOutEditText;
	// private Button mSendButton;

	// Name of the connected device
	private String mConnectedDeviceName = null;
	// Array adapter for the conversation thread
	// private ArrayAdapter<String> mConversationArrayAdapter;
	// String buffer for outgoing messages
	// private StringBuffer mOutStringBuffer;
	// Local Bluetooth adapter
	private BluetoothAdapter mBluetoothAdapter = null;
	// Member object for the chat services
	private BluetoothConnectService mChatService = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (D)
			Log.e(TAG, "+++ ON CREATE +++");

		// Set up the window layout
		setContentView(R.layout.doodle_together);

		// requestWindowFeature(Window.FEATURE_NO_TITLE);

		drawView = new MyDrawView(this);
		ViewGroup v = (ViewGroup) findViewById(R.id.linear_draw_layout);
		// View v = (View) findViewById(R.id.draw_canvas);
		id = findId();
		drawView.setId(id);
		v.addView(drawView);
		drawView.requestFocus();

		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

	}

	// Returns a valid id that isn't in use
	public int findId() {
		View v = findViewById(id);
		while (v != null) {
			v = findViewById(++id);
		}
		return id++;
	}

	@Override
	public void onStart() {
		super.onStart();
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		// TODO

		if (D)
			Log.e(TAG, "++ ON START ++");

		// If BT is not on, request that it be enabled.
		// setupChat() will then be called during onActivityResult
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
			// Otherwise, setup the chat session
		} else {
			if (mChatService == null)
				// TODO setup paint
				setupChat();
			;
		}
	}

	@Override
	public synchronized void onResume() {
		// Button clear = (Button) findViewById(R.id.clear_button);
		// clear.setOnClickListener(new OnClickListener() {

		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// MyDrawView drawscreen = (DrawView) BluetoothConnect.this
		// .findViewById(id);
		// drawscreen.clearScreen();
		// }
		// });
		super.onResume();
		if (D)
			Log.e(TAG, "+ ON RESUME +");

		// Performing this check in onResume() covers the case in which BT was
		// not enabled during onStart(), so we were paused to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mChatService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mChatService.getState() == BluetoothConnectService.STATE_NONE) {
				// Start the Bluetooth chat services
				mChatService.start();
				toast= true;
			}
		}
	}

	private void setupChat() {
	
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread

		// Initialize the send button with a listener that for click events
		// mSendButton = (Button) findViewById(R.id.button_send);
		// mSendButton.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// // Send a message using content of the edit text widget
		// TextView view = (TextView) findViewById(R.id.edit_text_out);
		// String message = view.getText().toString();
		// sendMessage(message);
		// }
		// });

		// Initialize the BluetoothConnectService to perform bluetooth
		// connections
		if (mChatService == null) {
			System.out.println(mChatService);
		}

		mChatService = new BluetoothConnectService(this, mHandler);

		// Initialize the buffer for outgoing messages
		// mOutStringBuffer = new StringBuffer("");
	}

	@Override
	public synchronized void onPause() {
		super.onPause();
		if (D)
			Log.e(TAG, "- ON PAUSE -");
	}

	@Override
	public void onStop() {
		super.onStop();
		if (D)
			Log.e(TAG, "-- ON STOP --");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Stop the Bluetooth chat services
		if (mChatService != null)
			mChatService.stop();
		if (D)
			Log.e(TAG, "--- ON DESTROY ---");
	}

	private void ensureDiscoverable() {
		if (D)
			Log.d(TAG, "ensure discoverable");
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	/**
	 * Sends a message.
	 * NOT USED 
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(MyPath path, Paint paint) {
		
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothConnectService.STATE_CONNECTED && toast) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			toast = false;
			return;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		MesgBluetooth message = new MesgBluetooth(path);
		if (path.isEmpty()) {
			Toast.makeText(BluetoothConnect.this,
					"sending empty message path for move", Toast.LENGTH_SHORT)
					.show();
		}
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(path);// writing path directly
			byte[] send = baos.toByteArray();

			// byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void sendMessage2(MyMotionEvent motion) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothConnectService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			View con_status= findViewById(R.id.con_status);
			//con_status.    .setText("not connected ");
			return;
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;

		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(motion);// writing path directly
			byte[] send = baos.toByteArray();

			// byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void clearScreen(View d) {
		MyDrawView v = (MyDrawView) findViewById(id);
		v.clear();
		Log.d("clear", "clearScreenExecuted");
	}

	@TargetApi(11)
	private final void setStatus(int resId) {
		final ActionBar actionBar = getActionBar();
		if (actionBar == null) {
			System.out.println("actionbar is null");
		}
		actionBar.setSubtitle(resId);
	}

	@TargetApi(11)
	private final void setStatus(CharSequence subTitle) {
		final ActionBar actionBar = getActionBar();
		actionBar.setSubtitle(subTitle);
	}

	@SuppressLint("ParserError")
	static Pattern messagePattern = Pattern
			.compile("\\(([0-9\\.]+)\\,([0-9\\.]+)\\)");

	// The Handler that gets information back from the BluetoothConnectService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				if (D)
					Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
				switch (msg.arg1) {
				case BluetoothConnectService.STATE_CONNECTED:
					setStatus(getString(R.string.title_connected_to,
							mConnectedDeviceName));
					// mConversationArrayAdapter.clear();
					break;
				case BluetoothConnectService.STATE_CONNECTING:
					setStatus(R.string.title_connecting);
					break;
				case BluetoothConnectService.STATE_LISTEN:
				case BluetoothConnectService.STATE_NONE:
					setStatus(R.string.title_not_connected);
					break;
				}
				break;
			// case MESSAGE_WRITE:
			// byte[] writeBuf = (byte[]) msg.obj;
			// // construct a string from the buffer
			// String writeMessage = new String(writeBuf);
			//
			// // mConversationArrayAdapter.add("Me:  " + writeMessage);
			// break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				ObjectInputStream ois;
				try {
					ois = new ObjectInputStream(new ByteArrayInputStream(
							readBuf));
					// MesgBluetooth mesg = (MesgBluetooth) ois.readObject();
					MyMotionEvent mesgPath = (MyMotionEvent) ois.readObject();
					// Path src= mesgPath.src;
					// MyPath mesgPath= mesg.getPath();
					// if (src.isEmpty()){
					// Toast.makeText(BluetoothConnect.this,
					// "empty message path", Toast.LENGTH_SHORT).show();
					// }
					ois.close();
					System.out.println("readMessage=" + mesgPath.toString());

					MyDrawView v = (MyDrawView) findViewById(id);
					MyPath test = new MyPath();
					if (mesgPath.isClear()) {
						v.mBitmap.eraseColor(Color.TRANSPARENT);
						v.invalidate();
						System.gc();
					} else {
						//v.otherPaint= mesgPath.paint;
						v.draw(mesgPath.x, mesgPath.y, mesgPath.action);
					}
					break;
				} catch (StreamCorruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (D)
			Log.d(TAG, "onActivityResult " + resultCode);

		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE_SECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, true);
				toast= true;
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				Log.d("resultcode= RESULT_OK",
						"java bluetoooth connect line 356");
				connectDevice(data, false);
				toast= true;
			}
			break;
		case REQUEST_CODE:
			int[] color = new int[3];
			if (resultCode == RESULT_OK) {
				color[0] = (int) (data
						.getDoubleArrayExtra("somitsolutions.training.android.colorpicker.color_of_the_shape"))[0];
				color[1] = (int) (data
						.getDoubleArrayExtra("somitsolutions.training.android.colorpicker.color_of_the_shape"))[1];
				color[2] = (int) (data
						.getDoubleArrayExtra("somitsolutions.training.android.colorpicker.color_of_the_shape"))[2];
				int value = Color.rgb(color[0], color[1], color[2]);
				((MyDrawView) findViewById(id)).changeColor(value);
				
				
				Intent serverIntent = new Intent(this, DeviceListActivity.class);//to connect back after connection lost 
				Log.d("Request connect", "connection request to be made");
				startActivityForResult(serverIntent,
						REQUEST_CONNECT_DEVICE_INSECURE);
				Log.d("connection", "request made - started activity for result");

			}
		case PICKED_COLOR:
			setupChat();
		case REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode == Activity.RESULT_OK) {
				// Bluetooth is now enabled, so set up a chat session
				// setupChat();

				// TODO
				// setup paint
				setupChat();

			} else {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean secure) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		if (mChatService == null) {
			Log.d("mChatSerive", "mchatService is null");
		}
		mChatService.connect(device, secure);
		Log.d("connectDevice()", "bluetooth connect 390 connection made");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent serverIntent = null;
		switch (item.getItemId()) {
		case R.id.secure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
			return true;
		case R.id.insecure_connect_scan:
			// Launch the DeviceListActivity to see devices and do scan
			serverIntent = new Intent(this, DeviceListActivity.class);
			Log.d("Request connect", "connection request to be made");
			startActivityForResult(serverIntent,
					REQUEST_CONNECT_DEVICE_INSECURE);
			Log.d("connection", "request made - started activity for result");
			return true;
		case R.id.discoverable:
			// Ensure this device is discoverable by others
			ensureDiscoverable();
			return true;
		}
		return false;
	}
	
	public void launchColor(View v) {
		launchColorPicker();
	
	}

	private void launchColorPicker() {
		Intent launchcolorpicker = new Intent(this, ColorPicker.class);
//		launchcolorpicker.setClassName(
//				"somitsolutions.training.android.colorpicker",
//				"somitsolutions.training.android.colorpicker.ColorPicker");
		launchcolorpicker
				.setAction("somitsolutions.training.android.colorpicker.android.intent.action.COLORPICKER");
		launchcolorpicker.addCategory("CATEGORY_DEFAULT");
		//launchcolorpicker
		//	.setType("vnd.somitsolutions.color/vnd.somitsolutions.color-value");

		try {
			startActivityForResult(launchcolorpicker, REQUEST_CODE);

		} catch (ActivityNotFoundException e) {
			Log.e("IntentExample", "Activity could not be started...");
		}
	}

	class MyDrawView extends View {

		private Bitmap mBitmap;
		private Canvas mCanvas;
		private MyPath mPath;
		private MyPath otherPath;
		private Paint mBitmapPaint;
		private Paint mPaint;
		private Paint otherPaint;

		public MyDrawView(Context c) {
			super(c);
			this.setBackgroundColor(Color.WHITE);

			mPath = new MyPath();
			otherPath = new MyPath();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setDither(true);
			mPaint.setColor(0xFF000000);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(3);
			
			otherPaint = new Paint();
			otherPaint.setAntiAlias(true);
			otherPaint.setDither(true);
			otherPaint.setColor(0xFF000000);
			otherPaint.setStyle(Paint.Style.STROKE);
			otherPaint.setStrokeJoin(Paint.Join.ROUND);
			otherPaint.setStrokeCap(Paint.Cap.ROUND);
			otherPaint.setStrokeWidth(3);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
		}

		@Override
		protected synchronized void onDraw(Canvas canvas) {
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
			// mergedPath.addPath(mPath);
			// mergedPath.addPath(otherPath);
			// canvas.drawPath(mergedPath, mPaint);
			synchronized (this) {
				canvas.drawPath(otherPath, mPaint);
				canvas.drawPath(mPath, mPaint);

			}

		}

		private float mX, mY;
		private float otherX, otherY;
		private static final float TOUCH_TOLERANCE = 2;

		private synchronized void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private synchronized void touch_start_other(float x, float y) {
			otherPath.reset();
			otherPath.moveTo(x, y);
			otherX = x;
			otherY = y;
		}

		private synchronized void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				if (mPath.isEmpty()) {
					Toast.makeText(BluetoothConnect.this,
							"sending empty message path for move",
							Toast.LENGTH_SHORT).show();
				}

				mX = x;
				mY = y;
			}
		}

		private synchronized void touch_move_other(float x, float y) {
			float dx = Math.abs(x - otherX);
			float dy = Math.abs(y - otherY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				otherPath.quadTo(otherX, otherY, (x + otherX) / 2,
						(y + otherY) / 2);

				otherX = x;
				otherY = y;
			}
		}

		private synchronized void touch_up() {
			mPath.lineTo(mX, mY);
			// BluetoothConnect.this.sendMessage(mPath,mPaint);
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
		}

		private synchronized void touch_up_other() {
			otherPath.lineTo(otherX, otherY);

			// commit the path to our offscreen
			mCanvas.drawPath(otherPath, mPaint);
			// kill this so we don't double draw
			otherPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {

			float x = event.getX();
			float y = event.getY();
			MyMotionEvent motion = new MyMotionEvent(x, y, event.getAction(),mPaint);
			int action = event.getAction();
			BluetoothConnect.this.sendMessage2(motion);
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				touch_start(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;
			}
			return true;
		}

		public boolean draw(float x, float y, int action) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				otherPath.reset();
				touch_start_other(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move_other(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up_other();
				invalidate();
				break;
			}
			return true;
		}

		public void clear() {
			mBitmap.eraseColor(Color.TRANSPARENT);
			invalidate();
			System.gc();
			MyMotionEvent motion = new MyMotionEvent(0, 0, 0,mPaint);
			motion.setClear(true);
			BluetoothConnect.this.sendMessage2(motion);

		}
		
		public void changeColor(int value){
			mPaint.setColor(value);	
		}
		
	}

}

// class DrawView extends View implements OnTouchListener {
// private static final String TAG = "DrawView";
//
// List<Point> points = new ArrayList<Point>();
// Paint paint = new Paint();
//

//
// public DrawView(Context context) {
// super(context);
// setFocusable(true);
// setFocusableInTouchMode(true);
// setBackgroundColor(Color.WHITE);
// this.setOnTouchListener(this);
//
// paint.setColor(Color.BLACK);
// paint.setAntiAlias(true);
// Log.d("drawview", "drawview created from inner class");
// }
//
// @Override
// public void onDraw(Canvas canvas) {
// for (Point point : points) {
// canvas.drawCircle(point.x, point.y, 10, paint);
// // Log.d(TAG, "Painting: "+point);
// }
// }
//
// public boolean onTouch(View view, MotionEvent event) {
// // if (event.getAction() != MotionEvent.ACTION_DOWN)
// // return super.onTouchEvent(event);
// Point point = new Point();
// point.x = event.getX();
// point.y = event.getY();
// int u = event.getHistorySize();
// points.add(point);
// BluetoothConnect.this.sendMessage(point.toString());
// for (int i = 0; i < u; i++) {
// point = new Point();
// point.x = event.getHistoricalX(i);
// point.y = event.getHistoricalY(i);
// points.add(point);
// BluetoothConnect.this.sendMessage(point.toString());
// }
// invalidate();
// // Log.d("drawn", "drew ontouch"+point.toString());
// // Log.d("message sent", "message sent to other device!!!!");
// // Log.d(TAG, "point: " + point);
// return true;
// }
// }
//
// class Point {
// float x, y;
//
// @Override
// public String toString() {
// return "(" + x + "," + y + ")";
// }
// }