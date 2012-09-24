package com.jisha.DoodleTogether;

import java.util.ArrayList;
import java.util.List;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DrawView extends View implements OnTouchListener {
	private static final String TAG = "DrawView";

	List<Point> points = new ArrayList<Point>();
	private Paint paint = new Paint();

	public DrawView(Context context) {
		super(context);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setBackgroundColor(Color.WHITE);

		this.setOnTouchListener(this);

		paint.setColor(Color.BLACK);
		//paint.setAntiAlias(true);
	}
	
	public void changeColor(int value){
		paint.setColor(value);
		Log.d("color","colorchanged to"+value);
	}

	@Override
	public void onDraw(Canvas canvas) {
		for (Point point : points) {
			canvas.drawCircle(point.x, point.y, 10, paint);
			Log.d("Paint", "Paint value= "+paint.getColor());
			
		}
	}

	public boolean onTouch(View view, MotionEvent event) {
		// if (event.getAction() != MotionEvent.ACTION_DOWN)
		// return super.onTouchEvent(event);
		Point point = new Point();
		point.x = event.getX();
		point.y = event.getY();
		int u = event.getHistorySize();
		points.add(point);
		for (int i = 0; i < u; i++) {
			point = new Point();
			point.x = event.getHistoricalX(i);
			point.y = event.getHistoricalY(i);
			points.add(point);
		}
		invalidate();
		// Log.d("drawn", "drew ontouch"+point.toString());
		// Log.d("message sent", "message sent to other device!!!!");
		// Log.d(TAG, "point: " + point);
		return true;
	}
	
	public void clearScreen() {
		points = new ArrayList<Point>();
		invalidate();
		Log.d("clear","clearScreenExecuted");
	}

	class Point {
		float x, y;

		@Override
		public String toString() {
			return x + ", " + y;
		}
	}
}