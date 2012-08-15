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

package com.jbh.DoodleTogether;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
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

	// Intent request codes
	private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
	private static final int REQUEST_ENABLE_BT = 3;

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
		Button clear = (Button) findViewById(R.id.clear_button);
		clear.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				DrawView drawscreen = (DrawView) BluetoothConnect.this
						.findViewById(id);
				drawscreen.clearScreen();
			}
		});

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
			}
		}
	}

	private void setupChat() {
		Log.d(TAG, "setupChat()");

		// Initialize the array adapter for the conversation thread

		// Initialize the compose field with a listener for the return key
		// mOutEditText = (EditText) findViewById(R.id.edit_text_out);
		// mOutEditText.setOnEditorActionListener(mWriteListener); //editor
		// listener commented out

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
	 * 
	 * @param message
	 *            A string of text to send.
	 */
	private void sendMessage(Path message) {
		// Check that we're actually connected before trying anything
		if (mChatService.getState() != BluetoothConnectService.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		// if (message.length() > 0) {
		// // Get the message bytes and tell the BluetoothConnectService to
		// // write
		// }

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(message);
			byte[] send = baos.toByteArray();

			// byte[] send = message.getBytes();
			mChatService.write(send);

			// Reset out string buffer to zero and clear the edit text field

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				// construct a string from the buffer
				String writeMessage = new String(writeBuf);
				// mConversationArrayAdapter.add("Me:  " + writeMessage);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				ObjectInputStream ois;
				try {
					ois = new ObjectInputStream(new ByteArrayInputStream(
							readBuf));
					Path mesgPath = (Path) ois.readObject();
					ois.close();
					System.out.println("readMessage=" + mesgPath.toString());

					MyDrawView v = (MyDrawView) findViewById(id);
					v.mPath.addPath(mesgPath);
					v.invalidate();
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

				// construct a string from the valid bytes in the buffer

				// if (readMessage.equals("clear")) {
				// System.out.println("readMessage is clear" + readMessage);
				// DrawView drawon = (DrawView) findViewById(id);
				//
				// drawon.invalidate();
				// break;
				// }

				// Matcher m = messagePattern.matcher(readMessage);
				// String mesg;

				// int i = readMessage.indexOf(",");
				// String xfloat = readMessage.substring(0, i);
				// String yfloat = readMessage.substring(i+1);
				// String x = xfloat.substring(0, xfloat.indexOf("."));
				// String y = readMessage.substring(0,yfloat.indexOf("."));

				// MyDrawView v = (MyDrawView) findViewById(id);
				//
				// drawView = new MyDrawView(this);
				// // View v=findViewById(R.id.linear_draw_layout);
				// // v.addView(drawView);
				// // drawView.requestFocus();
				//
				// Point point = new Point();
				// while (m.find()) {
				// String xfloat = m.group(1);
				// String yfloat = m.group(2);
				// try {
				// Log.d("Message received", xfloat + "," + yfloat);
				// float fx = Float.valueOf(xfloat.trim()).floatValue();// TODO
				// float fy = Float.valueOf(yfloat.trim()).floatValue();
				// point.x = fx;
				// point.y = fy;
				// } catch (IllegalArgumentException e) {
				// System.out
				// .println("Exception: point location not an integer");
				// e.printStackTrace();
				// }
				// v.points.add(point);
				// }
				// v.invalidate();
				// break;
				// TODO

				// case MESSAGE_CLEAR:
				// Log.d("MessageClear","message clear");
				// DrawView drawon = (DrawView) findViewById(id);
				// drawon.points= new ArrayList<Point>();
				// break;
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
			}
			break;
		case REQUEST_CONNECT_DEVICE_INSECURE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				Log.d("resultcode= RESULT_OK",
						"java bluetoooth connect line 356");
				connectDevice(data, false);
			}
			break;
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

	class MyDrawView extends View {

		private Bitmap mBitmap;
		private Canvas mCanvas;
		private Path mPath;
		private Paint mBitmapPaint;
		private Paint mPaint;

		public MyDrawView(Context c) {
			super(c);
			this.setBackgroundColor(Color.WHITE);

			mPath = new Path();
			mBitmapPaint = new Paint(Paint.DITHER_FLAG);

			mPaint = new Paint();
			mPaint.setAntiAlias(true);
			mPaint.setDither(true);
			mPaint.setColor(0xFF000000);
			mPaint.setStyle(Paint.Style.STROKE);
			mPaint.setStrokeJoin(Paint.Join.ROUND);
			mPaint.setStrokeCap(Paint.Cap.ROUND);
			mPaint.setStrokeWidth(3);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);
			
		}

		private float mX, mY;
		private static final float TOUCH_TOLERANCE = 2;

		private void touch_start(float x, float y) {
			mPath.reset();
			mPath.moveTo(x, y);
			mX = x;
			mY = y;
		}

		private void touch_move(float x, float y) {
			float dx = Math.abs(x - mX);
			float dy = Math.abs(y - mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				BluetoothConnect.this.sendMessage(mPath);
				mX = x;
				mY = y;
			}
		}

		private void touch_up() {
			mPath.lineTo(mX, mY);
			BluetoothConnect.this.sendMessage(mPath);
			// commit the path to our offscreen
			mCanvas.drawPath(mPath, mPaint);
			// kill this so we don't double draw
			mPath.reset();
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();

			switch (event.getAction()) {
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

		public void clear() {
			mBitmap.eraseColor(Color.TRANSPARENT);
			invalidate();
			System.gc();
		}
	}

}

// class DrawView extends View implements OnTouchListener {
// private static final String TAG = "DrawView";
//
// List<Point> points = new ArrayList<Point>();
// Paint paint = new Paint();
//
// public void clearScreen() {
// points = new ArrayList<Point>();
// invalidate();
// BluetoothConnect.this.sendMessage("clear");
// Log.d("clear","clearScreenExecuted");
// }
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