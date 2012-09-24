package com.jisha.DoodleTogether;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class MyDrawView extends View {

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
		
	}
	
	public void changeColor(int value){
		mPaint.setColor(value);	
	}
	
}