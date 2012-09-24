package com.jisha.DoodleTogether;

import java.io.Serializable;

import android.util.Log;

public class MesgBluetooth implements Serializable{
	 private static final long serialVersionUID = 0L;
	// private Paint paint;
	 private MyPath path;
	
	public MesgBluetooth( MyPath path){
		if (path.isEmpty()){
			Log.d("path","is empty");
		}
		//this.paint= paint;
		this.path=path;
	}
	 
//	public Paint getPaint() {
//		return paint;
//	}
//	public void setPaint(Paint paint) {
//		this.paint = paint;
//	}
	public MyPath getPath() {
		return path;
	}
	public void setPath(MyPath path) {
		this.path = path;
	}
}
