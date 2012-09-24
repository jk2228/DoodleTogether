package com.jisha.DoodleTogether;

import java.io.Serializable;

import android.graphics.Paint;

public class MyMotionEvent implements Serializable {
	 private static final long serialVersionUID = 1L;
	 public float x;
	 public float y;
	 public int action;
	 private boolean clearMessage;
	 public Paint paint;
	 
	 public MyMotionEvent(float x,float y,int action,Paint paint){
		 this.x=x;
		 this.y=y;
		 this.action=action;
		 clearMessage=false;
		 this.paint=null;
	 }
	 
	 public void setClear(boolean clear){
		 clearMessage= clear;
	 }
	 
	 public boolean isClear(){
		 return clearMessage;
	 }
	 
}
